package br.com.tscode.checking.platform.background.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** Precise / approximate / denied — mirrors Android's fine-vs-coarse location grant. */
enum class LocationStatus { PRECISE, IMPRECISE, DENIED }

/**
 * Snapshot of every permission/service the "Status de Permissões" report shows.
 * Computed by [PermissionsInspector.inspect].
 */
data class PermissionsStatus(
    val location: LocationStatus,
    val cameraMicGranted: Boolean,
    val autoStartEnabled: Boolean,
    val batteryRestricted: Boolean,   // true = NOT exempt from Doze (i.e. restricted)
    val backgroundGranted: Boolean,   // "Allow all the time" background location
    val notificationsGranted: Boolean,
)

/**
 * Reads the live Android permission/service state for the Permissions screen.
 *
 * Notes on the trickier ones:
 *  - **Auto-Start** has no query API. On stock Android (OemType.GENERIC) the
 *    RECEIVE_BOOT_COMPLETED receiver works by default, so it reports enabled.
 *    On restrictive OEMs (Xiaomi/Oppo/…) detection is impossible, so we fall back
 *    to a user-acknowledged flag the dialog persists once it has opened the OEM screen.
 *  - **Background** maps to ACCESS_BACKGROUND_LOCATION — the permission that actually
 *    lets the 15-min / geofence engine run while the app is backgrounded.
 *  - **Exact Alarm** is the precise resume at the end of a Scheduled Pause window.
 */
object PermissionsInspector {

    fun inspect(context: Context, oemAutoStartAcknowledged: Boolean): PermissionsStatus {
        val fine = context.granted(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = context.granted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val location = when {
            fine -> LocationStatus.PRECISE
            coarse -> LocationStatus.IMPRECISE
            else -> LocationStatus.DENIED
        }

        val ladder = PermissionLadder.checkStatus(context)

        val autoStart = if (PermissionLadder.detectOemType() == OemType.GENERIC) {
            true
        } else {
            oemAutoStartAcknowledged
        }

        return PermissionsStatus(
            location = location,
            cameraMicGranted = context.granted(Manifest.permission.CAMERA) &&
                context.granted(Manifest.permission.RECORD_AUDIO),
            autoStartEnabled = autoStart,
            batteryRestricted = !ladder.batteryOptExempt,
            backgroundGranted = ladder.backgroundLocationGranted,
            notificationsGranted = ladder.notificationsGranted,
        )
    }

    private fun Context.granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
