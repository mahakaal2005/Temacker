package com.example.temacker.feature_auth.data.repository

import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_auth.domain.model.User
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Dummy stand-in for FirebaseAuthRemoteDataSource-backed repository, until google-services.json
// is available. Same AuthRepository contract, so swapping it out later is a one-line DI change.
class InMemoryAuthRepository(
    private val session: SessionManager
) : AuthRepository {

    private val dummyUser = User(
        uid = "dummy-uid-1",
        displayName = "Asha Rao",
        email = "asha.rao@example.com",
        photoUrl = null
    )

    private val _user = MutableStateFlow<User?>(null)

    override fun observeUser() = _user.asStateFlow()

    override suspend fun signInWithGoogle(): Result<User, DataError> {
        delay(600) // simulate network latency
        _user.value = dummyUser
        session.setSession(dummyUser.uid)
        return Result.Success(dummyUser)
    }

    override suspend fun signOut() {
        _user.value = null
        session.setSession(null)
    }
}
