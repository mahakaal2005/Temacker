package com.example.temacker.core.data.notification

import android.util.Log
import com.example.temacker.core.data.firebase.safeFirestoreCall
import com.example.temacker.core.domain.notification.PushTokenRegistrar
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.onFailure
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "FcmTokenRegistrar"
private const val WRITE_TIMEOUT_MS = 5_000L

class FirebaseFcmTokenRegistrar(
    private val messaging: FirebaseMessaging,
    private val firestore: FirebaseFirestore,
    private val session: SessionManager
) : PushTokenRegistrar {

    override suspend fun register() {
        val token = currentToken() ?: return
        onNewToken(token)
    }

    override suspend fun onNewToken(token: String) {
        val uid = session.getUid() ?: return
        // Offline the write queues in Firestore and never acks, so cap the wait.
        withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            safeFirestoreCall {
                tokenDoc(uid, token).set(mapOf("token" to token, "updatedAt" to Timestamp.now())).await()
            }.onFailure { Log.w(TAG, "onNewToken: could not save token ($it)") }
        }
    }

    // Must run while still signed in, or the fcmTokens rule denies the delete.
    override suspend fun unregister() {
        val uid = session.getUid() ?: return
        val token = currentToken() ?: return
        withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            safeFirestoreCall { tokenDoc(uid, token).delete().await() }
                .onFailure { Log.w(TAG, "unregister: could not delete token ($it)") }
        }
        // A fresh token is minted on next sign-in, so a signed-out device gets no pushes.
        try {
            messaging.deleteToken().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "unregister: deleteToken failed", e)
        }
    }

    private fun tokenDoc(uid: String, token: String) =
        firestore.collection("users").document(uid).collection("fcmTokens").document(token)

    private suspend fun currentToken(): String? = try {
        messaging.token.await()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "currentToken: no FCM token available", e)
        null
    }
}
