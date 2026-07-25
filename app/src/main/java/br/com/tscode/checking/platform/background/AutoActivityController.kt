package br.com.tscode.checking.platform.background

import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.tscode.checking.i18n.resolveEffectiveLanguageCode
import br.com.tscode.checking.platform.background.notifications.AutoActivityNotifications

internal enum class AutoActivityServiceStartResult {
    REQUESTED,
    DEFERRED_MISSING_FINE_LOCATION,
    DEFERRED_MISSING_BACKGROUND_LOCATION,
    DEFERRED_LOCATION_DISABLED,
    REJECTED_BY_SYSTEM,
}

// Single entry point for starting and stopping the background automatic-activities engine.
// Used by the UI toggle (T3B.5), BootReceiver (T3B.8), and WorkManager watchdog (T3B.8).
// Plain object — callers pass Context; no DI required.
object AutoActivityController {

    /**
     * Requests the foreground service only when its runtime prerequisites match [origin].
     *
     * The service repeats the same validation and catches promotion failures in onStartCommand(),
     * because permissions/app visibility can change after this method returns. The watchdog remains
     * enqueued when a request is deferred so the engine can recover after permissions/settings change.
     */
    internal fun start(
        context: Context,
        origin: AutoActivityServiceStartOrigin,
    ): AutoActivityServiceStartResult {
        val prerequisites = inspectAutoActivityServicePrerequisites(context)
        val serviceAlreadyRunning = AutoActivityForegroundService.isRunning
        val blockReason =
            autoActivityServiceStartBlockReason(
                prerequisites = prerequisites,
                origin = origin,
                serviceAlreadyRunning = serviceAlreadyRunning,
            )
        if (
            shouldWarnBackgroundLocationRequired(
                prerequisites = prerequisites,
                origin = origin,
                serviceAlreadyRunning = serviceAlreadyRunning,
            )
        ) {
            AutoActivityNotifications.postBackgroundLocationRequiredNotification(
                context,
                resolveEffectiveLanguageCode(null),
            )
        } else if (prerequisites.backgroundLocationGranted) {
            AutoActivityNotifications.cancelBackgroundLocationRequiredNotification(context)
        }
        val result =
            if (blockReason != null) {
                blockReason.toStartResult()
            } else {
                val intent =
                    Intent(context, AutoActivityForegroundService::class.java)
                        .putExtra(EXTRA_AUTO_ACTIVITY_START_ORIGIN, origin.wireValue)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    AutoActivityServiceStartResult.REQUESTED
                } catch (_: RuntimeException) {
                    // Includes ForegroundServiceStartNotAllowedException on Android 12+.
                    // The final defensive guard remains inside the service for asynchronous failures.
                    AutoActivityServiceStartResult.REJECTED_BY_SYSTEM
                }
            }
        AutoActivityWatchdogWorker.enqueue(context)
        return result
    }

    // Stop the foreground service gracefully, cancel the WorkManager watchdog,
    // and remove all registered geofences.
    fun stop(context: Context) {
        val intent = Intent(context, AutoActivityForegroundService::class.java).apply {
            action = AutoActivityForegroundService.ACTION_STOP
        }
        // Each cleanup is independent. A service-start restriction must not prevent the
        // watchdog from being cancelled or stale project geofences from being removed.
        runCatching { context.startService(intent) }
        runCatching { AutoActivityWatchdogWorker.cancel(context) }
        AutoActivityNotifications.cancelBackgroundLocationRequiredNotification(context)
        GeofenceManager.unregisterAll(context)
    }

    internal fun clearBackgroundLocationWarning(context: Context) {
        AutoActivityNotifications.cancelBackgroundLocationRequiredNotification(context)
    }

    // True while the service is alive.  Tracks the onCreate/onDestroy lifecycle
    // via AutoActivityForegroundService.isRunning.
    fun isRunning(): Boolean = AutoActivityForegroundService.isRunning
}

private fun AutoActivityServiceStartBlockReason.toStartResult(): AutoActivityServiceStartResult =
    when (this) {
        AutoActivityServiceStartBlockReason.MISSING_FINE_LOCATION ->
            AutoActivityServiceStartResult.DEFERRED_MISSING_FINE_LOCATION

        AutoActivityServiceStartBlockReason.MISSING_BACKGROUND_LOCATION ->
            AutoActivityServiceStartResult.DEFERRED_MISSING_BACKGROUND_LOCATION

        AutoActivityServiceStartBlockReason.LOCATION_DISABLED ->
            AutoActivityServiceStartResult.DEFERRED_LOCATION_DISABLED
    }
