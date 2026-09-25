package com.example.temacker.feature_auth.data.remote

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRemoteDataSource {
    fun observeUser(): Flow<User?>
    suspend fun signInWithGoogle(): Result<User, DataError>
    suspend fun signInWithEmail(email: String, password: String): Result<User, DataError>
    suspend fun registerWithEmail(email: String, password: String): Result<User, DataError>
    suspend fun signOut()
    suspend fun deleteAccount(): EmptyResult<DataError>
}
