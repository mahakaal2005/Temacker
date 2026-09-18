package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.data.firebase.safeFirestoreCall
import com.example.temacker.core.data.firebase.snapshots
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.data.mapper.toFirestoreMap
import com.example.temacker.feature_project.data.mapper.toProject
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreProjectRemoteDataSource(
    private val firestore: FirebaseFirestore
) : ProjectRemoteDataSource {

    override fun observeUserProjects(userId: String): Flow<List<Project>> =
        firestore.collectionGroup("members")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                // Member docs don't store their own projectId — it's implicit in the doc path
                // (projects/{projectId}/members/{userId}) — so read it from the parent reference.
                val projectIds = snapshot.documents.mapNotNull { it.reference.parent.parent?.id }
                // Membership list changing re-fetches the (small, rarely-changing) project docs.
                projectIds.mapNotNull { id ->
                    firestore.collection(PROJECTS).document(id).get().await().toProject()
                }
            }

    override fun observeProject(projectId: String): Flow<Project?> =
        firestore.collection(PROJECTS).document(projectId).snapshots().map { it.toProject() }

    override suspend fun createProjectWithLeader(
        name: String,
        ownerUid: String,
        ownerDisplayName: String,
        ownerPhotoUrl: String?
    ): Result<Pair<Project, Membership>, DataError> = safeFirestoreCall {
        val projectRef = firestore.collection(PROJECTS).document()
        val leaderRoleRef = projectRef.collection(ROLES).document()
        val defaultRoleRef = projectRef.collection(ROLES).document()
        val memberRef = projectRef.collection(MEMBERS).document(ownerUid)
        val createdAt = System.currentTimeMillis()

        val project = Project(id = projectRef.id, name = name, ownerUid = ownerUid, createdAt = createdAt)
        val leaderRole = Role(leaderRoleRef.id, project.id, "Leader", RolePermissions.ALL_GRANTED, isLeader = true)
        val defaultRole = Role(defaultRoleRef.id, project.id, "Default", RolePermissions.NONE, isLeader = false)
        val leaderMembership = Membership(
            projectId = project.id,
            userId = ownerUid,
            roleId = leaderRole.id,
            roleName = leaderRole.name,
            permissions = leaderRole.permissions,
            displayName = ownerDisplayName,
            photoUrl = ownerPhotoUrl,
            joinedAt = createdAt,
            isLeader = true
        )
        // defaultRoleId is a data-layer join key for the join-project transaction, not part of the domain model.
        val projectFields = project.toFirestoreMap() + mapOf("defaultRoleId" to defaultRole.id)

        firestore.batch()
            .set(projectRef, projectFields)
            .set(leaderRoleRef, leaderRole.toFirestoreMap())
            .set(defaultRoleRef, defaultRole.toFirestoreMap())
            .set(memberRef, leaderMembership.toFirestoreMap())
            .commit()
            .await()

        project to leaderMembership
    }

    companion object {
        const val PROJECTS = "projects"
        const val ROLES = "roles"
        const val MEMBERS = "members"
    }
}
