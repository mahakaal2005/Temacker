package com.example.temacker.feature_auth.data.remote

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.temacker.R
import com.example.temacker.core.data.activity.CurrentActivityHolder
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_auth.data.mapper.toUser
import com.example.temacker.feature_auth.domain.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.SecureRandom

// Gets the Google ID token from Credential Manager, then exchanges it with Firebase Auth.
class FirebaseAuthRemoteDataSource(
    private val context: Context,
    private val currentActivityHolder: CurrentActivityHolder,
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth
) : AuthRemoteDataSource {

    override fun observeUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser?.toUser()) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(): Result<User, DataError> {
        val activity = currentActivityHolder.activity ?: run {
            Log.e(TAG, "signInWithGoogle: no foreground Activity to attach Credential Manager to")
            return Result.Error(DataError.Network.UNKNOWN)
        }

        // Google returns this hash back in the ID token's nonce claim — hash it (not the raw
        // value) per Google's Credential Manager guidance, so a replayed token can't be reused.
        val hashedNonce = hashNonce(generateNonce())

        return try {
            // Try silently for an account that has already signed into this app before.
            val credential = try {
                credentialManager.getCredential(
                    activity,
                    buildRequest(filterByAuthorizedAccounts = true, hashedNonce = hashedNonce)
                ).credential
            } catch (e: NoCredentialException) {
                // First-time user — fall back to the full account picker (sign-up).
                // Use GetSignInWithGoogleOption for better "button click" flow handling.
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetSignInWithGoogleOption.Builder(context.getString(R.string.google_web_client_id))
                            .setNonce(hashedNonce)
                            .build()
                    )
                    .build()
                credentialManager.getCredential(activity, request).credential
            }

            val idToken = credential.toGoogleIdToken() ?: run {
                Log.e(TAG, "signInWithGoogle: credential was not a Google ID token, type=${credential.type}")
                return Result.Error(DataError.Network.UNKNOWN)
            }
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val firebaseUser = firebaseAuth.signInWithCredential(authCredential).await().user ?: run {
                Log.e(TAG, "signInWithGoogle: Firebase returned a null user after sign-in")
                return Result.Error(DataError.Network.UNKNOWN)
            }
            Result.Success(firebaseUser.toUser())
        } catch (e: GetCredentialCancellationException) {
            // User dismissed the picker — not an error, just stop.
            Log.e(TAG, "signInWithGoogle: GetCredentialCancellationException (message=${e.message})", e)
            Result.Error(DataError.Network.UNKNOWN) // LoginViewModel handles generic errors as non-fatal UI state
        } catch (e: GetCredentialException) {
            Log.e(TAG, "signInWithGoogle: Credential Manager failed (type=${e::class.simpleName})", e)
            Result.Error(DataError.Network.UNKNOWN)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "signInWithGoogle: Google ID token parsing failed", e)
            Result.Error(DataError.Network.UNKNOWN)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogle: Firebase sign-in failed", e)
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User, DataError> {
        return try {
            val user = firebaseAuth.signInWithEmailAndPassword(email, password).await().user ?: run {
                Log.e(TAG, "signInWithEmail: Firebase returned a null user after sign-in")
                return Result.Error(DataError.Network.UNKNOWN)
            }
            Result.Success(user.toUser())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail: failed", e)
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<User, DataError> {
        return try {
            val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user ?: run {
                Log.e(TAG, "registerWithEmail: Firebase returned a null user after registration")
                return Result.Error(DataError.Network.UNKNOWN)
            }
            try {
                user.sendEmailVerification().await()
            } catch (e: Exception) {
                // Account was still created successfully — verification email is a nice-to-have.
                Log.e(TAG, "registerWithEmail: account created but verification email failed to send", e)
            }
            Result.Success(user.toUser())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "registerWithEmail: failed", e)
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    private fun buildRequest(filterByAuthorizedAccounts: Boolean, hashedNonce: String): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(context.getString(R.string.google_web_client_id))
            .setNonce(hashedNonce)
            .build()
        return GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }

    private fun hashNonce(nonce: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(nonce.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun Credential.toGoogleIdToken(): String? {
        if (this !is CustomCredential || type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) return null
        return GoogleIdTokenCredential.createFrom(data).idToken
    }

    companion object {
        private const val TAG = "FirebaseAuthRemote"
    }
}
