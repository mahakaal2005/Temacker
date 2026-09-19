package com.example.temacker.feature_tasks.domain.model

import com.example.temacker.core.domain.util.DataError

const val MAX_REPLAY_ATTEMPTS = 5

// WAIT: still offline, keep the row untouched. RETRY: counts an attempt. FAIL: rejected, surfaced to the user.
enum class ReplayVerdict { WAIT, RETRY, FAIL }

fun replayVerdict(error: DataError, attempts: Int): ReplayVerdict = when (error) {
    DataError.Network.NO_INTERNET,
    DataError.Network.REQUEST_TIMEOUT,
    DataError.Network.SERVICE_UNAVAILABLE,
    DataError.Network.CANCELLED -> ReplayVerdict.WAIT
    DataError.Network.SERVER_ERROR,
    DataError.Network.TOO_MANY_REQUESTS,
    DataError.Network.UNKNOWN,
    DataError.Local.UNKNOWN -> if (attempts + 1 >= MAX_REPLAY_ATTEMPTS) ReplayVerdict.FAIL else ReplayVerdict.RETRY
    else -> ReplayVerdict.FAIL
}
