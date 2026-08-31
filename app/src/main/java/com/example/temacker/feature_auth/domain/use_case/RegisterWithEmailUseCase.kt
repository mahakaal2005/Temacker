package com.example.temacker.feature_auth.domain.use_case

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_auth.domain.model.User
import com.example.temacker.feature_auth.domain.repository.AuthRepository

class RegisterWithEmailUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User, DataError> =
        repository.registerWithEmail(email, password)
}
