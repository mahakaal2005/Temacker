package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.data.firebase.safeFirestoreCall
import com.example.temacker.core.data.firebase.snapshots
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.data.mapper.toFirestoreMap
import com.example.temacker.feature_project.data.mapper.toMembership
import com.example.temacker.feature_project.data.mapper.toRolePermissions
import com.example.temacker.feature_project.domain.model.Membership
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
    ): Result<Membership, DataError> = safeFirestoreCall {
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
            membership
        }.await()
    }

    override suspend fun removeMember(projectId: String, userId: String): EmptyResult<DataError> =
        safeFirestoreCall {
            membersRef(projectId).document(userId).delete().await()
            Unit
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
