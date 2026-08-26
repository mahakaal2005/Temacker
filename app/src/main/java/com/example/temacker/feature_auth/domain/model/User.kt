package com.example.temacker.feature_auth.domain.model

data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?
)
