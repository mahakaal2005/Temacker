package com.example.temacker.feature_auth.data.mapper

import com.example.temacker.feature_auth.domain.model.User
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toUser() = User(
    uid = uid,
    displayName = displayName ?: email.orEmpty(),
    email = email.orEmpty(),
    photoUrl = photoUrl?.toString()
)
