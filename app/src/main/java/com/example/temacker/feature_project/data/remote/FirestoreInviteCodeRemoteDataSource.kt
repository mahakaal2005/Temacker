package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.data.firebase.safeFirestoreCall
import com.example.temacker.core.data.firebase.snapshots
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.data.mapper.toFirestoreMap
import com.example.temacker.feature_project.data.mapper.toInviteCode
import com.example.temacker.feature_project.domain.model.InviteCode
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirestoreInviteCodeRemoteDataSource(
    private val firestore: FirebaseFirestore
) : InviteCodeRemoteDataSource {

    override fun observeActiveInviteCode(projectId: String): Flow<InviteCode?> =
        firestore.collection("inviteCodes")
            .whereEqualTo("projectId", projectId)
            .whereEqualTo("isActive", true)
            .limit(1)
            .snapshots()
            .map { snapshot -> snapshot.documents.firstOrNull()?.toInviteCode() }

    override suspend fun generateInviteCode(projectId: String): Result<InviteCode, DataError> = safeFirestoreCall {
        // Deactivate any existing active code first so a project only ever has one live code.
        val existing = firestore.collection("inviteCodes")
            .whereEqualTo("projectId", projectId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
        val batch = firestore.batch()
        existing.documents.forEach { batch.update(it.reference, "isActive", false) }

        val code = generateCode()
        val expiresAt = System.currentTimeMillis() + INVITE_CODE_TTL_MS
        val inviteCode = InviteCode(code = code, projectId = projectId, expiresAt = expiresAt, isActive = true)
        batch.set(firestore.collection("inviteCodes").document(code), inviteCode.toFirestoreMap())
        batch.commit().await()

        inviteCode
    }

    // "TMK-XXXX-XX" per the design spec (specs/UI/temacker-all-phases-android-concept.html).
    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no ambiguous 0/O/1/I
        fun block(len: Int) = (1..len).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "TMK-${block(4)}-${block(2)}"
    }

    companion object {
        private const val INVITE_CODE_TTL_MS = 7L * 24 * 60 * 60 * 1000 // 7 days, per the spec.
    }
}
