package br.com.tscode.checking.platform.background.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * The 5 steps of the "Revisar Permissões" ladder (§23.6).
 * Steps 1–4 are required; step 5 is advisory (OEM autostart guidance).
 */
enum class LadderStep {
    POST_NOTIFICATIONS,    // Step 1 — Android 13+; needed for FGS + activity notifications
    FINE_LOCATION,         // Step 2 — ACCESS_FINE_LOCATION (+ coarse bundled)
    BACKGROUND_LOCATION,   // Step 3 — "Allow all the time"; must route to settings on API 29+
    BATTERY_OPTIMIZATION,  // Step 4 — exemption from Doze; ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    OEM_GUIDANCE,          // Step 5 — advisory; deep-link or guide to OEM autostart screen
}

/**
 * OEM families with known hostile battery / autostart restrictions.
 * GENERIC = stock Android; no special guidance needed.
 */
enum class OemType { GENERIC, SAMSUNG, XIAOMI, MOTOROLA, HUAWEI, OPPO, VIVO }

/**
 * Snapshot of all permission states at a moment in time.
 * Construct via [PermissionLadder.checkStatus].
 *
 * @param oemGuidanceShown caller persists this across the session; once shown it counts as satisfied
 */
data class PermissionLadderStatus(
    val notificationsGranted: Boolean,
    val fineLocationGranted: Boolean,
    val backgroundLocationGranted: Boolean,
    val batteryOptExempt: Boolean,
    val oemGuidanceShown: Boolean,
) {
    /**
     * Minimum required to START / keep the engine running (P2): notifications (API 33+) + precise
     * (fine) location. Background ("Allow all the time") and battery exemption are RECOMMENDED —
     * they improve background reliability but must NOT block the engine from starting.
     */
    val minimumToStartGranted: Boolean
        get() = notificationsGranted && fineLocationGranted

    /** All recommended permissions granted → full background reliability (steps 1–4). */
    val allRecommendedGranted: Boolean
        get() = notificationsGranted &&
                fineLocationGranted &&
                backgroundLocationGranted &&
                batteryOptExempt

    /** @deprecated alias of [allRecommendedGranted]; prefer the explicit names. */
    val allRequiredGranted: Boolean
        get() = allRecommendedGranted

    /**
     * The first unsatisfied step, in ladder order. The ladder still GUIDES the user through every
     * step (incl. background + battery) even though only [minimumToStartGranted] is needed to start.
     * Returns null only when all steps are satisfied AND either OEM is GENERIC or guidance was shown.
     */
    val nextStep: LadderStep?
        get() = when {
            !notificationsGranted -> LadderStep.POST_NOTIFICATIONS
            !fineLocationGranted -> LadderStep.FINE_LOCATION
            !backgroundLocationGranted -> LadderStep.BACKGROUND_LOCATION
            !batteryOptExempt -> LadderStep.BATTERY_OPTIMIZATION
            !oemGuidanceShown && PermissionLadder.detectOemType() != OemType.GENERIC ->
                LadderStep.OEM_GUIDANCE
            else -> null
        }
}

/**
 * Static helpers for the "Revisar Permissões" permission ladder (§23.6, T3B.4).
 *
 * This object only checks and launches — it does not hold state.
 * The Compose sequencer in AutoActivitiesDialog (T3B.5) drives the actual request flow;
 * it uses [checkStatus] to decide the next step, then calls the appropriate launcher here
 * or uses ActivityResultLauncher for in-dialog permission dialogs.
 */
object PermissionLadder {

    /**
     * Returns a [PermissionLadderStatus] reflecting the current Android permission state.
     *
     * @param oemGuidanceShown pass true once the OEM guidance screen has been shown in this session
     */
    fun checkStatus(context: Context, oemGuidanceShown: Boolean = false): PermissionLadderStatus =
        PermissionLadderStatus(
            notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                context.hasPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            else true,
            fineLocationGranted = context.hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION),
            backgroundLocationGranted = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                    context.hasPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                else ->
                    context.hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            },
            batteryOptExempt = isIgnoringBatteryOptimizations(context),
            oemGuidanceShown = oemGuidanceShown,
        )

    fun isIgnoringBatteryOptimizations(context: Context): Boolean =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)

    /**
     * On Android 11+ the OS does not allow requesting ACCESS_BACKGROUND_LOCATION via a
     * runtime dialog. Route the user to the app's settings page where they can choose
     * "Allow all the time". (§23.6 step 3 rationale.)
     */
    fun launchLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    /**
     * Launches the battery-optimization exemption dialog (step 4).
     * Falls back to generic battery-saver settings if the direct action is unavailable.
     */
    fun launchBatteryOptimizationRequest(context: Context) {
        val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (context.packageManager.resolveActivity(direct, 0) != null) {
            runCatching { context.startActivity(direct) }
        } else {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                )
            }
        }
    }

    /**
     * Opens the app's notification settings screen directly (Android 8+), where the user
     * reviews/toggles notification permission and channels. Falls back to app details.
     */
    fun launchAppNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        if (context.packageManager.resolveActivity(intent, 0) != null) {
            runCatching { context.startActivity(intent) }
        } else {
            launchAppPermissionSettings(context)
        }
    }

    /**
     * Opens the app's permission settings page.
     * Use this when a permission is permanently denied so the user can re-enable it manually.
     */
    fun launchAppPermissionSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    /**
     * Infers the OEM family from [Build.MANUFACTURER].
     * Used for step 5 — whether to show OEM autostart guidance.
     */
    fun detectOemType(): OemType = when (Build.MANUFACTURER.lowercase().trim()) {
        "samsung" -> OemType.SAMSUNG
        "xiaomi", "redmi", "poco" -> OemType.XIAOMI
        "motorola", "moto" -> OemType.MOTOROLA
        "huawei", "honor" -> OemType.HUAWEI
        "oppo", "realme", "oneplus" -> OemType.OPPO
        "vivo" -> OemType.VIVO
        else -> OemType.GENERIC
    }

    /**
     * Returns true if a known deep-link intent to the OEM autostart screen is resolvable
     * on this device. Used to decide whether to show a "tap here to open settings" button
     * or just advisory text.
     */
    fun canDeepLinkToOemAutostart(context: Context): Boolean =
        oemAutostartIntent(detectOemType())?.let { intent ->
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        } ?: false

    /**
     * Attempts to deep-link to the OEM autostart / "no background restrictions" settings.
     * These intents are undocumented and may stop working after OEM firmware updates —
     * treat as best-effort.
     */
    fun launchOemAutostartSettings(context: Context) {
        val intent = oemAutostartIntent(detectOemType()) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    // ─── Internal ────────────────────────────────────────────────────────────

    /**
     * Returns a best-effort deep-link Intent for the given OEM's autostart/restrictions screen,
     * or null for GENERIC/unknown.
     */
    internal fun oemAutostartIntent(oem: OemType): Intent? = when (oem) {
        OemType.SAMSUNG -> Intent().apply {
            component = ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.battery.ui.BatteryActivity",
            )
        }
        OemType.XIAOMI -> Intent().apply {
            component = ComponentName(
                "com.miui.powerkeeper",
                "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity",
            )
        }
        OemType.HUAWEI -> Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity",
            )
        }
        OemType.OPPO -> Intent().apply {
            component = ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            )
        }
        OemType.VIVO -> Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            )
        }
        // Motorola and generic Android are AOSP-based; no special screen needed
        OemType.MOTOROLA, OemType.GENERIC -> null
    }

    private fun Context.hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
