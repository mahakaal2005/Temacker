package com.example.temacker.feature_auth.domain.use_case

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_auth.domain.model.User
import com.example.temacker.feature_auth.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<User, DataError> = repository.signInWithGoogle()
}
