package br.com.tscode.checking.platform.background

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

// Single entry point for starting and stopping the background automatic-activities engine.
// Used by the UI toggle (T3B.5), BootReceiver (T3B.8), and WorkManager watchdog (T3B.8).
// Plain object — callers pass Context; no DI required.
object AutoActivityController {

    // Start the foreground service and enqueue the WorkManager watchdog.
    // On API 31+ the OS may throw ForegroundServiceStartNotAllowedException when the app
    // is in the background; the watchdog will restart the FGS at its next scheduled tick.
    fun start(context: Context) {
        // A location-typed FGS requires ACCESS_FINE_LOCATION at runtime; on Android 14+ starting it
        // without that permission throws (MissingForegroundServiceType/SecurityException). start() is
        // also called by the watchdog and BootReceiver, which don't pre-check permissions, so guard
        // here. The watchdog is still (re)enqueued so the engine auto-recovers on a later tick once
        // the user grants precise location.
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFineLocation) {
            val intent = Intent(context, AutoActivityForegroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // ForegroundServiceStartNotAllowedException (API 31+, started from background) or
                // similar. Swallow — the WorkManager watchdog restarts the service at its next tick
                // when the app is in a foreground/allowed state.
            }
        }
        AutoActivityWatchdogWorker.enqueue(context)
    }

    // Stop the foreground service gracefully, cancel the WorkManager watchdog,
    // and remove all registered geofences.
    fun stop(context: Context) {
        val intent = Intent(context, AutoActivityForegroundService::class.java).apply {
            action = AutoActivityForegroundService.ACTION_STOP
        }
        context.startService(intent)
        AutoActivityWatchdogWorker.cancel(context)
        GeofenceManager.unregisterAll(context)
    }

    // True while the service is alive.  Tracks the onCreate/onDestroy lifecycle
    // via AutoActivityForegroundService.isRunning.
    fun isRunning(): Boolean = AutoActivityForegroundService.isRunning
}
