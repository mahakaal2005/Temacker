package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.data.firebase.safeFirestoreCall
import com.example.temacker.core.data.firebase.snapshots
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.data.mapper.toFirestoreMap
import com.example.temacker.feature_project.data.mapper.toMembership
import com.example.temacker.feature_project.data.mapper.toProject
import com.example.temacker.feature_project.data.mapper.toRolePermissions
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Project
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreMembershipRemoteDataSource(
    private val firestore: FirebaseFirestore
) : MembershipRemoteDataSource {

    private fun membersRef(projectId: String) =
        firestore.collection("projects").document(projectId).collection("members")

    override fun observeMembers(projectId: String): Flow<List<Membership>> =
        membersRef(projectId).snapshots().map { snapshot -> snapshot.documents.mapNotNull { it.toMembership(projectId) } }

    override fun observeMembership(projectId: String, userId: String): Flow<Membership?> =
        membersRef(projectId).document(userId).snapshots().map { it.toMembership(projectId) }

    @Suppress("UNCHECKED_CAST")
    override suspend fun joinProject(
        code: String,
        userId: String,
        displayName: String,
        photoUrl: String?
    ): Result<Pair<Membership, Project>, DataError> = safeFirestoreCall {
        val codeRef = firestore.collection("inviteCodes").document(code)
        firestore.runTransaction { txn ->
            val codeSnap = txn.get(codeRef)
            if (!codeSnap.exists()) {
                throw FirebaseFirestoreException("Invalid invite code", FirebaseFirestoreException.Code.NOT_FOUND)
            }
            val projectId = codeSnap.getString("projectId")
                ?: throw FirebaseFirestoreException("Invalid invite code", FirebaseFirestoreException.Code.NOT_FOUND)
            val isActive = codeSnap.getBoolean("isActive") ?: false
            val expiresAt = codeSnap.getLong("expiresAt")
            if (!isActive || (expiresAt != null && expiresAt < System.currentTimeMillis())) {
                throw FirebaseFirestoreException("Invite code expired", FirebaseFirestoreException.Code.FAILED_PRECONDITION)
            }

            val projectRef = firestore.collection("projects").document(projectId)
            val projectSnap = txn.get(projectRef)
            val defaultRoleId = projectSnap.getString("defaultRoleId")
                ?: throw FirebaseFirestoreException("Project misconfigured", FirebaseFirestoreException.Code.NOT_FOUND)
            val roleRef = projectRef.collection("roles").document(defaultRoleId)
            val roleSnap = txn.get(roleRef)
            val roleName = roleSnap.getString("name") ?: "Default"
            val permissions = (roleSnap.get("permissions") as? Map<String, Any?> ?: emptyMap()).toRolePermissions()
            val isLeader = roleSnap.getBoolean("isLeader") ?: false

            val memberRef = projectRef.collection("members").document(userId)
            if (txn.get(memberRef).exists()) {
                throw FirebaseFirestoreException("Already a member", FirebaseFirestoreException.Code.ALREADY_EXISTS)
            }
            val joinedAt = System.currentTimeMillis()
            // The invite's creator is who added this member; codes older than Phase 5 have none.
            val membership = Membership(
                projectId = projectId,
                userId = userId,
                roleId = defaultRoleId,
                roleName = roleName,
                permissions = permissions,
                displayName = displayName,
                photoUrl = photoUrl,
                joinedAt = joinedAt,
                isLeader = isLeader,
                roleSetByUid = codeSnap.getString("createdByUid"),
                roleSetByDisplayName = codeSnap.getString("createdByDisplayName"),
                roleSetAt = joinedAt
            )
            txn.set(memberRef, membership.toFirestoreMap())
            val project = projectSnap.toProject()
                ?: throw FirebaseFirestoreException("Project misconfigured", FirebaseFirestoreException.Code.NOT_FOUND)
            membership to project
        }.await()
    }

    override suspend fun removeMember(projectId: String, userId: String): EmptyResult<DataError> =
        safeFirestoreCall {
            membersRef(projectId).document(userId).delete().await()
            Unit
        }

    override suspend fun transferLeadership(
        projectId: String,
        fromUid: String,
        fromDisplayName: String,
        toUid: String
    ): Result<List<Membership>, DataError> = safeFirestoreCall {
        val projectRef = firestore.collection("projects").document(projectId)
        val rolesRef = projectRef.collection("roles")
        val defaultRoleId = projectRef.get().await().getString("defaultRoleId")
            ?: throw FirebaseFirestoreException("Project misconfigured", FirebaseFirestoreException.Code.NOT_FOUND)
        val leaderRole = rolesRef.whereEqualTo("isLeader", true).limit(1).get().await().documents.firstOrNull()
            ?: throw FirebaseFirestoreException("Leader role missing", FirebaseFirestoreException.Code.NOT_FOUND)
        val defaultRole = rolesRef.document(defaultRoleId).get().await()
        if (!defaultRole.exists()) {
            throw FirebaseFirestoreException("Default role missing", FirebaseFirestoreException.Code.NOT_FOUND)
        }

        val stamp = mapOf(
            "roleSetByUid" to fromUid,
            "roleSetByDisplayName" to fromDisplayName,
            "roleSetAt" to System.currentTimeMillis()
        )
        val toRef = membersRef(projectId).document(toUid)
        val fromRef = membersRef(projectId).document(fromUid)
        // Role name/permissions are copied straight from the role docs so they match what the rules compare against.
        firestore.batch()
            .update(
                toRef,
                mapOf(
                    "roleId" to leaderRole.id,
                    "roleName" to leaderRole.getString("name"),
                    "permissions" to leaderRole.get("permissions"),
                    "isLeader" to true
                ) + stamp
            )
            .update(
                fromRef,
                mapOf(
                    "roleId" to defaultRole.id,
                    "roleName" to defaultRole.getString("name"),
                    "permissions" to defaultRole.get("permissions"),
                    "isLeader" to false,
                    // Tells the rules which member the same batch promotes, so a demotion alone is refused.
                    "transferToUid" to toUid
                ) + stamp
            )
            .commit()
            .await()

        listOfNotNull(toRef.get().await().toMembership(projectId), fromRef.get().await().toMembership(projectId))
    }

    override suspend fun reassignRole(projectId: String, userId: String, roleId: String, byUid: String, byDisplayName: String): Result<Membership, DataError> =
        safeFirestoreCall {
            val roleRef = firestore.collection("projects").document(projectId).collection("roles").document(roleId)
            val roleSnap = roleRef.get().await()
            val roleName = roleSnap.getString("name")
                ?: throw FirebaseFirestoreException("Role not found", FirebaseFirestoreException.Code.NOT_FOUND)
            val permissions = (roleSnap.get("permissions") as? Map<String, Any?> ?: emptyMap()).toRolePermissions()
            val isLeader = roleSnap.getBoolean("isLeader") ?: false
            val memberRef = membersRef(projectId).document(userId)
            memberRef.update(
                mapOf(
                    "roleId" to roleId,
                    "roleName" to roleName,
                    "permissions" to permissions.toFirestoreMap(),
                    "isLeader" to isLeader,
                    "roleSetByUid" to byUid,
                    "roleSetByDisplayName" to byDisplayName,
                    "roleSetAt" to System.currentTimeMillis()
                )
            ).await()
            val updated = memberRef.get().await().toMembership(projectId)
                ?: throw FirebaseFirestoreException("Member not found", FirebaseFirestoreException.Code.NOT_FOUND)
            updated
        }
}
