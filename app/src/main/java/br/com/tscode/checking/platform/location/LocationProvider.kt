package br.com.tscode.checking.platform.location

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

sealed class LocationCapture {
    data class Success(val lat: Double, val lon: Double, val accuracyMeters: Double) : LocationCapture()
    object Timeout : LocationCapture()
    object Unavailable : LocationCapture()
}

@Singleton
class LocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
) {
    companion object {
        private const val TIME_BUDGET_MS = 15_000L
    }

    @SuppressLint("MissingPermission")
    suspend fun capture(accuracyThresholdMeters: Int): LocationCapture {
        val bestRef = AtomicReference<android.location.Location?>(null)

        return try {
            withTimeout(TIME_BUDGET_MS) {
                callbackFlow<LocationCapture> {
                    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
                        .setMinUpdateIntervalMillis(500L)
                        .build()

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { loc ->
                                val current = bestRef.get()
                                if (isBetter(loc, current)) bestRef.set(loc)
                                val best = bestRef.get() ?: return
                                if (best.accuracy.isFinite() && best.accuracy <= accuracyThresholdMeters) {
                                    trySend(LocationCapture.Success(best.latitude, best.longitude, best.accuracy.toDouble()))
                                    close()
                                }
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                    awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
                }.first()
            }
        } catch (_: TimeoutCancellationException) {
            val best = bestRef.get()
            if (best != null) {
                LocationCapture.Success(best.latitude, best.longitude, best.accuracy.toDouble())
            } else {
                LocationCapture.Timeout
            }
        } catch (_: Exception) {
            LocationCapture.Unavailable
        }
    }

    private fun isBetter(candidate: android.location.Location, current: android.location.Location?): Boolean {
        if (current == null) return true
        if (!candidate.accuracy.isFinite()) return false
        if (!current.accuracy.isFinite()) return true
        return when {
            candidate.accuracy < current.accuracy -> true
            candidate.accuracy > current.accuracy -> false
            else -> candidate.time > current.time
        }
    }
}
