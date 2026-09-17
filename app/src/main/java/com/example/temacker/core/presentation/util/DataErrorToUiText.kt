package com.example.temacker.core.presentation.util

import com.example.temacker.R
import com.example.temacker.core.domain.util.DataError

fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Network.UNAUTHORIZED -> UiText.StringResource(R.string.error_unauthorized)
        DataError.Network.SERVER_ERROR -> UiText.StringResource(R.string.error_server)
        DataError.Network.PERMISSION_DENIED -> UiText.StringResource(R.string.error_permission_denied)
        DataError.Network.CONFLICT -> UiText.StringResource(R.string.error_invite_code_invalid)
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.error_request_timeout)
        DataError.Network.SERIALIZATION -> UiText.StringResource(R.string.error_serialization)
        DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(R.string.error_too_many_requests)
        DataError.Network.BAD_REQUEST -> UiText.StringResource(R.string.error_bad_request)
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
