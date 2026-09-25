package com.example.temacker.feature_auth.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeUser(): Flow<User?>
    suspend fun signInWithGoogle(): Result<User, DataError>
    suspend fun signInWithEmail(email: String, password: String): Result<User, DataError>
    suspend fun registerWithEmail(email: String, password: String): Result<User, DataError>
    suspend fun signOut()
    // Deletes the Firebase Auth user itself — callers are responsible for leaving projects first
    // (feature_profile's DeleteAccountUseCase), since AuthRepository can't depend on feature_project.
    suspend fun deleteAccount(): EmptyResult<DataError>
}
