package com.example.temacker.feature_tasks.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.temacker.core.domain.util.DataError
import org.junit.jupiter.api.Test

class ReplayVerdictTest {

    @Test
    fun `offline style errors wait without spending an attempt`() {
        listOf(
            DataError.Network.NO_INTERNET, DataError.Network.REQUEST_TIMEOUT,
            DataError.Network.SERVICE_UNAVAILABLE, DataError.Network.CANCELLED
        ).forEach { assertThat(replayVerdict(it, attempts = 0)).isEqualTo(ReplayVerdict.WAIT) }
    }

    @Test
    fun `rejections fail straight away`() {
        listOf(
            DataError.Network.CONFLICT, DataError.Network.PERMISSION_DENIED,
            DataError.Network.NOT_FOUND, DataError.Network.UNAUTHORIZED
        ).forEach { assertThat(replayVerdict(it, attempts = 0)).isEqualTo(ReplayVerdict.FAIL) }
    }

    @Test
    fun `server errors retry until the attempt cap`() {
        assertThat(replayVerdict(DataError.Network.SERVER_ERROR, attempts = 0)).isEqualTo(ReplayVerdict.RETRY)
        assertThat(replayVerdict(DataError.Network.UNKNOWN, attempts = MAX_REPLAY_ATTEMPTS - 2)).isEqualTo(ReplayVerdict.RETRY)
        assertThat(replayVerdict(DataError.Network.SERVER_ERROR, attempts = MAX_REPLAY_ATTEMPTS - 1)).isEqualTo(ReplayVerdict.FAIL)
    }
}
