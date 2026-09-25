package com.example.temacker.feature_profile.domain.model

import com.example.temacker.core.domain.util.DataError

sealed interface DeleteAccountResult {
    data object Success : DeleteAccountResult
    // The Leader role/membership can never be deleted or reassigned (Phase 1 rule) — a Leader has
    // to run Succession first, in every project named here, before their account can go.
    data class BlockedByLeadership(val projectNames: List<String>) : DeleteAccountResult
    data class Failed(val error: DataError) : DeleteAccountResult
}
