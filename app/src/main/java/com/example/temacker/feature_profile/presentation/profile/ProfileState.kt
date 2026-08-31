package com.example.temacker.feature_profile.presentation.profile

data class ProfileState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val projectName: String = "",
    val roleName: String = "",
    val memberSince: String = "",
    val isLoading: Boolean = true
)
