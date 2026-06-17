package br.com.tscode.checking.platform.background

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.repository.CheckRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Registers the backend's coarse geofence circles with the Android GeofencingClient (§23.2, T3B.9).
// Geofences are wake-up triggers only — precise matching is always server-side (Approach A).
// Circles come from GET /check/geofences (1-hour TTL cache in CheckRepositoryImpl).
@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkRepository: CheckRepository,
) {

    // Fetch the current project's circles and register them with Play Services.
    // Idempotent: addGeofences replaces any existing set keyed by request ID.
    // Swallows all errors — FGS timer + WorkManager watchdog are the primary path.
    @SuppressLint("MissingPermission")
    suspend fun register(chave: String) {
        val circles = when (val r = checkRepository.getGeofences(chave)) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return
        }
        if (circles.isEmpty()) return

        val geofences = circles.map { circle ->
            Geofence.Builder()
                .setRequestId(circle.id.toString())
                .setCircularRegion(
                    circle.centerLat,
                    circle.centerLng,
                    circle.radiusMeters.toFloat(),
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT,
                )
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        runCatching {
            suspendCancellableCoroutine<Unit> { cont ->
                LocationServices.getGeofencingClient(context)
                    .addGeofences(request, geofencePendingIntent(context))
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }.onSuccess {
            android.util.Log.i(TAG, "Registered ${geofences.size} geofence(s) with Play Services")
        }.onFailure {
            android.util.Log.w(TAG, "Geofence registration failed", it)
        }
    }

    companion object {
        private const val TAG = "GeofenceManager"
        private const val REQUEST_CODE_GEOFENCE = 3001

        // Removes all previously registered geofences for this app.
        // Called from AutoActivityController.stop() — must be a static call (no DI needed).
        fun unregisterAll(context: Context) {
            runCatching {
                LocationServices.getGeofencingClient(context)
                    .removeGeofences(geofencePendingIntent(context))
            }
        }

        private fun geofencePendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, REQUEST_CODE_GEOFENCE, intent, flags)
        }
    }
}
