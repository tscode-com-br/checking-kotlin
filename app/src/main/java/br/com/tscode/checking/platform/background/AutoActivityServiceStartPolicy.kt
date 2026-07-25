package br.com.tscode.checking.platform.background

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

internal const val EXTRA_AUTO_ACTIVITY_START_ORIGIN =
    "br.com.tscode.checking.extra.AUTO_ACTIVITY_START_ORIGIN"

/**
 * Identifies who requested the location foreground service.
 *
 * Android 14+ treats a location foreground service requested while the app is in the background
 * differently from one requested by a visible Activity. Keeping the origin explicit avoids relying
 * on a stale process-level visibility flag. A missing/unknown value is treated conservatively as a
 * system restart because old PendingIntents and START_STICKY restarts carry no trusted UI context.
 */
internal enum class AutoActivityServiceStartOrigin(
    val wireValue: String,
    val requiresBackgroundLocation: Boolean,
) {
    USER_VISIBLE("user_visible", false),
    BOOT("boot", true),
    WATCHDOG("watchdog", true),
    GEOFENCE("geofence", true),
    SCHEDULED_PAUSE("scheduled_pause", true),
    TASK_REMOVED("task_removed", true),
    SYSTEM_RESTART("system_restart", true),
    ;

    companion object {
        fun fromWireValue(value: String?): AutoActivityServiceStartOrigin =
            entries.firstOrNull { it.wireValue == value } ?: SYSTEM_RESTART
    }
}

internal data class AutoActivityServicePrerequisites(
    val sdkInt: Int,
    val fineLocationGranted: Boolean,
    val backgroundLocationGranted: Boolean,
    val locationEnabled: Boolean,
)

internal enum class AutoActivityServiceStartBlockReason {
    MISSING_FINE_LOCATION,
    MISSING_BACKGROUND_LOCATION,
    LOCATION_DISABLED,
}

/**
 * Whether a background recreation should remind the user to grant "Allow all the time".
 *
 * Android introduced the separate permission in API 29. On API 29–33 the reminder is advisory and
 * does not alter the existing start behavior; on API 34+ [autoActivityServiceStartBlockReason]
 * additionally defers an unsafe background promotion.
 */
internal fun shouldWarnBackgroundLocationRequired(
    prerequisites: AutoActivityServicePrerequisites,
    origin: AutoActivityServiceStartOrigin,
    serviceAlreadyRunning: Boolean = false,
): Boolean =
    !serviceAlreadyRunning &&
        prerequisites.sdkInt >= Build.VERSION_CODES.Q &&
        prerequisites.fineLocationGranted &&
        prerequisites.locationEnabled &&
        !prerequisites.backgroundLocationGranted &&
        origin.requiresBackgroundLocation

/**
 * Returns why a start must be deferred, or null when it is safe to request the service.
 *
 * The background-location gate is intentionally limited to Android 14+, where the platform
 * validates while-in-use permissions during startForeground() and throws SecurityException.
 * Earlier releases retain the app's existing degraded-mode behavior.
 */
internal fun autoActivityServiceStartBlockReason(
    prerequisites: AutoActivityServicePrerequisites,
    origin: AutoActivityServiceStartOrigin,
    serviceAlreadyRunning: Boolean = false,
): AutoActivityServiceStartBlockReason? =
    when {
        // A command delivered to an already-promoted service does not create a new location FGS.
        // This keeps project/geofence refreshes and scheduled-pause commands working even if a
        // runtime prerequisite changes temporarily. A future service instance validates again.
        serviceAlreadyRunning -> null

        !prerequisites.fineLocationGranted ->
            AutoActivityServiceStartBlockReason.MISSING_FINE_LOCATION

        prerequisites.sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            !prerequisites.locationEnabled ->
            AutoActivityServiceStartBlockReason.LOCATION_DISABLED

        prerequisites.sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            origin.requiresBackgroundLocation &&
            !prerequisites.backgroundLocationGranted ->
            AutoActivityServiceStartBlockReason.MISSING_BACKGROUND_LOCATION

        else -> null
    }

internal fun inspectAutoActivityServicePrerequisites(context: Context): AutoActivityServicePrerequisites {
    val fineLocationGranted =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    val backgroundLocationGranted =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            fineLocationGranted
        }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val locationEnabled =
        locationManager != null &&
            runCatching { LocationManagerCompat.isLocationEnabled(locationManager) }
                .getOrDefault(false)

    return AutoActivityServicePrerequisites(
        sdkInt = Build.VERSION.SDK_INT,
        fineLocationGranted = fineLocationGranted,
        backgroundLocationGranted = backgroundLocationGranted,
        locationEnabled = locationEnabled,
    )
}

internal sealed interface AutoActivityForegroundPromotion {
    data object Succeeded : AutoActivityForegroundPromotion

    data class Rejected(
        val cause: RuntimeException,
    ) : AutoActivityForegroundPromotion
}

/**
 * Converts platform foreground-service rejections into data so the Service can stop cleanly.
 *
 * Android documents several RuntimeException subtypes here (including SecurityException and
 * ForegroundServiceStartNotAllowedException). Keeping the try/catch immediately around the
 * promotion also closes the race between a caller's permission check and onStartCommand().
 */
internal fun attemptAutoActivityForegroundPromotion(promote: () -> Unit): AutoActivityForegroundPromotion =
    try {
        promote()
        AutoActivityForegroundPromotion.Succeeded
    } catch (error: RuntimeException) {
        AutoActivityForegroundPromotion.Rejected(error)
    }
