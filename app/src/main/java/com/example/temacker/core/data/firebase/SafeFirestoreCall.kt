package com.example.temacker.core.data.firebase

import android.util.Log
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException

private const val TAG = "SafeFirestoreCall"

// Catches Firestore exceptions at the layer that owns them (data layer) and maps to DataError.
suspend fun <T> safeFirestoreCall(action: suspend () -> T): Result<T, DataError> {
    return try {
        Result.Success(action())
    } catch (e: CancellationException) {
        throw e
    } catch (e: FirebaseFirestoreException) {
        val error = when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.Network.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE -> DataError.Network.NO_INTERNET
            // Invalid/expired invite code surfaces here — CONFLICT reads clearest to the caller.
            FirebaseFirestoreException.Code.FAILED_PRECONDITION,
            FirebaseFirestoreException.Code.NOT_FOUND -> DataError.Network.CONFLICT
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> DataError.Network.REQUEST_TIMEOUT
            else -> DataError.Network.UNKNOWN
        }
        Log.e(TAG, "safeFirestoreCall: Firestore call failed (code=${e.code})", e)
        Result.Error(error)
    } catch (e: Exception) {
        Log.e(TAG, "safeFirestoreCall: unexpected exception", e)
        Result.Error(DataError.Network.UNKNOWN)
    }
}
