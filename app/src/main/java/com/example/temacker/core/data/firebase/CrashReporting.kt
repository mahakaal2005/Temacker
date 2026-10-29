package com.example.temacker.core.data.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestoreException

// Sends a non-fatal to Crashlytics, skipping offline/timeout errors that are normal on a phone.
fun Throwable.reportIfUnexpected() {
    if (this is FirebaseFirestoreException &&
        (code == FirebaseFirestoreException.Code.UNAVAILABLE || code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED)
    ) return
    // runCatching keeps JVM unit tests (no FirebaseApp) from failing here.
    runCatching { FirebaseCrashlytics.getInstance().recordException(this) }
}