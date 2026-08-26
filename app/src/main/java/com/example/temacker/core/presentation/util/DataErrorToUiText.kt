package com.example.temacker.core.presentation.util

import com.example.temacker.R
import com.example.temacker.core.domain.util.DataError

fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Network.UNAUTHORIZED -> UiText.StringResource(R.string.error_unauthorized)
        DataError.Network.SERVER_ERROR -> UiText.StringResource(R.string.error_server)
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
