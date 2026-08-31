package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.data.firebase.safeFirestoreCall
import com.example.temacker.core.data.firebase.snapshots
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.data.mapper.toFirestoreMap
import com.example.temacker.feature_project.data.mapper.toRole
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRoleRemoteDataSource(
    private val firestore: FirebaseFirestore
) : RoleRemoteDataSource {

    private fun rolesRef(projectId: String) =
        firestore.collection("projects").document(projectId).collection("roles")

    override fun observeRoles(projectId: String): Flow<List<Role>> =
        rolesRef(projectId).snapshots().map { snapshot -> snapshot.documents.mapNotNull { it.toRole(projectId) } }

    override suspend fun createRole(projectId: String, name: String, permissions: RolePermissions): Result<Role, DataError> =
        safeFirestoreCall {
            val ref = rolesRef(projectId).document()
            val role = Role(id = ref.id, projectId = projectId, name = name, permissions = permissions, isLeader = false)
            ref.set(role.toFirestoreMap()).await()
            role
        }

    override suspend fun updateRole(projectId: String, roleId: String, name: String, permissions: RolePermissions): EmptyResult<DataError> =
        safeFirestoreCall {
            val role = Role(id = roleId, projectId = projectId, name = name, permissions = permissions, isLeader = false)
            rolesRef(projectId).document(roleId).set(role.toFirestoreMap()).await()
            Unit
        }

    override suspend fun deleteRole(projectId: String, roleId: String): EmptyResult<DataError> =
        safeFirestoreCall {
            rolesRef(projectId).document(roleId).delete().await()
            Unit
        }
}
