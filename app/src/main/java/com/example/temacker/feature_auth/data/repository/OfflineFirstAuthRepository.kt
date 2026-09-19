package com.example.temacker.feature_auth.data.repository

import com.example.temacker.core.domain.notification.PushTokenRegistrar
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_auth.data.remote.AuthRemoteDataSource
import com.example.temacker.feature_auth.domain.model.User
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

// Coordinates the Firebase remote source (auth state, sign-in) with the local session cache
// (DataStore), so SplashViewModel can route without waiting on Firebase's async listener.
class OfflineFirstAuthRepository(
    private val remote: AuthRemoteDataSource,
    private val session: SessionManager,
    private val pushTokens: PushTokenRegistrar
) : AuthRepository {

    override fun observeUser(): Flow<User?> = remote.observeUser()

    override suspend fun signInWithGoogle(): Result<User, DataError> {
        return remote.signInWithGoogle().onSuccess { user -> session.setSession(user.uid) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User, DataError> {
        return remote.signInWithEmail(email, password).onSuccess { user -> session.setSession(user.uid) }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<User, DataError> {
        return remote.registerWithEmail(email, password).onSuccess { user -> session.setSession(user.uid) }
    }

    override suspend fun signOut() {
        // Drop this device's push token while still authenticated.
        pushTokens.unregister()
        remote.signOut()
        session.setSession(null)
    }
}
