package com.example.temacker.core.domain.util

sealed interface DataError : Error {
    // PERMISSION_DENIED and CANCELLED are deliberate additions beyond the android-error-handling
    // skill's canonical set: PERMISSION_DENIED covers Firestore's distinct insufficient-permission
    // code, CANCELLED covers a dismissed Google sign-in picker (audit item #2) — both real cases the
    // skill's generic REST-shaped set doesn't anticipate.
    enum class Network : DataError {
        BAD_REQUEST, REQUEST_TIMEOUT, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, PERMISSION_DENIED,
        CONFLICT, TOO_MANY_REQUESTS, NO_INTERNET, PAYLOAD_TOO_LARGE, SERVER_ERROR,
        SERVICE_UNAVAILABLE, SERIALIZATION, CANCELLED, UNKNOWN
    }
    enum class Local : DataError { DISK_FULL, NOT_FOUND, UNKNOWN }
}
