package com.example.temacker.core.domain.util

sealed interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT, UNAUTHORIZED, PERMISSION_DENIED, CONFLICT,
        NO_INTERNET, SERVER_ERROR, SERIALIZATION, UNKNOWN
    }
    enum class Local : DataError { DISK_FULL, NOT_FOUND, UNKNOWN }
}
