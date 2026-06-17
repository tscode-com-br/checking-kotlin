package br.com.tscode.checking.presentation.settings.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.tscode.checking.platform.background.permissions.LocationStatus
import br.com.tscode.checking.platform.background.permissions.OemType
import br.com.tscode.checking.platform.background.permissions.PermissionLadder
import br.com.tscode.checking.platform.background.permissions.PermissionsInspector
import br.com.tscode.checking.platform.background.permissions.PermissionsStatus
import br.com.tscode.checking.presentation.components.DialogScaffold
import br.com.tscode.checking.presentation.components.PrimaryButton
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingLatestBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

private const val PERMISSIONS_PREFS = "checking_permissions"
private const val KEY_OEM_AUTOSTART_ACK = "oem_autostart_ack"

private val ToneGood = CheckingLatestBorder       // green
private val ToneWarn = Color(0xFFEA580C)          // orange
private val ToneBad = CheckingError               // red

/**
 * "Permissões" sub-dialog. Focused on what the app needs to run reliably in the background:
 *  - Precise location set to "Allow all the time".
 *  - One "Background Operation" button that requests the battery-optimization exemption and,
 *    on restrictive OEMs, the auto-start screen — in a single sequence.
 *  - Notifications.
 * Camera/microphone are NOT requested here; that permission is asked at the moment the user
 * records an accident video. The status report reflects background-readiness honestly (precise
 * location without "Allow all the time" is shown as a warning, since background won't work).
 */
@Composable
fun PermissionsDialog(
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = remember { context.getSharedPreferences(PERMISSIONS_PREFS, Context.MODE_PRIVATE) }

    var oemAutoStartAck by rememberSaveable {
        mutableStateOf(prefs.getBoolean(KEY_OEM_AUTOSTART_ACK, false))
    }
    var refreshKey by remember { mutableIntStateOf(0) }
    var showStatus by rememberSaveable { mutableStateOf(false) }
    // Second step of the "Background Operation" sequence: after the battery dialog returns,
    // open the OEM auto-start screen (restrictive OEMs only).
    val pendingOemStep = remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refreshKey++ }
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { refreshKey++ }

    // Re-read status whenever the user returns from an external settings screen, and advance
    // the Background Operation sequence (battery → OEM auto-start) on return.
    val currentRefresh by rememberUpdatedState(refreshKey)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey = currentRefresh + 1
                if (pendingOemStep.value) {
                    pendingOemStep.value = false
                    PermissionLadder.launchOemAutostartSettings(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val status: PermissionsStatus = remember(refreshKey, oemAutoStartAck) {
        PermissionsInspector.inspect(context, oemAutoStartAck)
    }

    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = t("permissions.title", null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )
        HorizontalDivider(color = CheckingDivider)

        // Precise location + "Allow all the time".
        PermissionSection(
            text = t("permissions.locationText", null),
            button = t("permissions.locationButton", null),
            onClick = {
                val fine = context.granted(Manifest.permission.ACCESS_FINE_LOCATION)
                val coarse = context.granted(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (!fine && !coarse) {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                } else {
                    // Already has some location — open settings so the user can choose
                    // "Allow all the time" + "Use precise location".
                    PermissionLadder.launchLocationSettings(context)
                }
            },
        )

        // One button for all the "stay alive in the background" permissions: battery-opt
        // exemption (dialog), then OEM auto-start (restrictive OEMs) on return.
        PermissionSection(
            text = t("permissions.backgroundOpsText", null),
            button = t("permissions.backgroundOpsButton", null),
            onClick = {
                if (PermissionLadder.detectOemType() != OemType.GENERIC) {
                    pendingOemStep.value = true
                    oemAutoStartAck = true
                    prefs.edit().putBoolean(KEY_OEM_AUTOSTART_ACK, true).apply()
                }
                PermissionLadder.launchBatteryOptimizationRequest(context)
            },
        )

        // Notifications.
        PermissionSection(
            text = t("permissions.notificationsText", null),
            button = t("permissions.notificationsButton", null),
            onClick = {
                val needsRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !context.granted(Manifest.permission.POST_NOTIFICATIONS)
                if (needsRequest) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    PermissionLadder.launchAppNotificationSettings(context)
                }
            },
        )

        // "pule duas linhas" before the status report
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = CheckingDivider)
        PrimaryButton(
            text = t("permissions.statusButton", null),
            onClick = {
                refreshKey++
                showStatus = !showStatus
            },
        )

        if (showStatus) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = t("permissions.statusTitle", null),
                    style = MaterialTheme.typography.labelMedium,
                    color = CheckingTextMuted,
                )

                // Location is only "ready" for background when it's PRECISE *and* granted
                // "Allow all the time"; precise-without-background is a warning, not success.
                val (locationValue, locationTone) = when {
                    status.location == LocationStatus.PRECISE && status.backgroundGranted ->
                        t("permissions.locationPrecise", null) to ToneGood
                    status.location == LocationStatus.PRECISE ->
                        t("permissions.locationPreciseNoBackground", null) to ToneWarn
                    status.location == LocationStatus.IMPRECISE ->
                        t("permissions.locationImprecise", null) to ToneWarn
                    else ->
                        t("permissions.locationDenied", null) to ToneBad
                }
                StatusRow(t("permissions.statusLocation", null), locationValue, locationTone)

                StatusRow(
                    label = t("permissions.statusBattery", null),
                    value = if (status.batteryRestricted) t("permissions.batteryRestricted", null)
                    else t("permissions.batteryUnrestricted", null),
                    tone = if (status.batteryRestricted) ToneBad else ToneGood,
                )
                StatusRow(
                    label = t("permissions.statusNotifications", null),
                    value = if (status.notificationsGranted) t("permissions.notificationsAllowed", null)
                    else t("permissions.notificationsDisallowed", null),
                    tone = if (status.notificationsGranted) ToneGood else ToneBad,
                )
            }
        }

        HorizontalDivider(color = CheckingDivider)
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(text = t("permissions.backButton", null), color = CheckingPrimary)
        }
    }
}

@Composable
private fun PermissionSection(
    text: String,
    button: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = CheckingTextMuted,
        )
        PrimaryButton(text = button, onClick = onClick)
    }
}

@Composable
private fun StatusRow(label: String, value: String, tone: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CheckingTextStrong,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = tone,
        )
    }
}

private fun Context.granted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
