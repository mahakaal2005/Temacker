package com.example.temacker.core.domain.model

data class HolderLoad(
    val holderUid: String,
    val holderDisplayName: String,
    val todoCount: Int,
    val doingCount: Int
) {
    val totalActive: Int get() = todoCount + doingCount
}
