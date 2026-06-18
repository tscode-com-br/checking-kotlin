package br.com.tscode.checking.presentation.settings.autoactivities

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.tscode.checking.platform.background.permissions.LocationStatus
import br.com.tscode.checking.platform.background.permissions.OemType
import br.com.tscode.checking.platform.background.permissions.PermissionLadder
import br.com.tscode.checking.platform.background.permissions.PermissionsInspector
import br.com.tscode.checking.presentation.components.DialogScaffold
import br.com.tscode.checking.presentation.components.PrimaryButton
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingLatestBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

/**
 * "Atividades Automáticas" sub-dialog (§23.7, T3B.5).
 *
 * Layout (top → bottom):
 *  1. Title "Atividades Automáticas"
 *  2. Explanatory paragraph
 *  3. Checkbox "Habilitar Atividades Automáticas"
 *  4. Button "Revisar Permissões" — only when checkbox is checked
 *  5. Permission denial notice — directly below the button, when applicable
 *  6. Button "Fechar"
 *
 * Permission ladder (§23.6) runs when "Revisar Permissões" is tapped:
 *   Step 0 — POST_NOTIFICATIONS (Android 13+)
 *   Step 1 — ACCESS_FINE_LOCATION + ACCESS_COARSE_LOCATION
 *   Step 2 — ACCESS_BACKGROUND_LOCATION (routes to location settings)
 *   Step 3 — Battery-optimization exemption (routes to system dialog)
 *   Step 4 — OEM guidance (advisory; shown in-dialog, then auto-advanced)
 *   Step 5 — DONE: evaluate and call onPermissionsGranted or onPermissionsDenied
 *
 * @param automaticActivitiesEnabled current persisted value of the toggle
 * @param onToggleChanged called when the user taps the checkbox
 * @param onPermissionsGranted called after the full ladder completes with all required granted
 * @param onPermissionsDenied called after the full ladder ends with at least one required denied
 * @param onDismiss called on back-press, scrim tap, or "Fechar"
 */
@Composable
fun AutoActivitiesDialog(
    automaticActivitiesEnabled: Boolean,
    permissionsSufficient: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // -1 = ladder not started; 0–4 = current step; 5 = evaluation complete
    var stepIndex by rememberSaveable { mutableIntStateOf(-1) }
    // True while we are waiting for the user to return from an external settings screen
    var isWaitingForResume by rememberSaveable { mutableStateOf(false) }
    // Advisory OEM step: once shown, counts as satisfied (§23.6 step 5)
    var oemGuidanceShown by rememberSaveable { mutableStateOf(false) }
    // Show the denial notice below "Revisar Permissões" after a failed ladder run
    var showPermissionsNotice by rememberSaveable { mutableStateOf(false) }

    // P4.1 — live permission checklist (additive). refreshKey re-reads the inspector on ON_RESUME and
    // after a per-row "fix" tap. This status-only observer never touches stepIndex / isWaitingForResume,
    // so it does NOT fight the ladder's step machine below.
    var refreshKey by remember { mutableIntStateOf(0) }
    var checklistOemAck by rememberSaveable { mutableStateOf(false) }
    val permStatus = remember(refreshKey, checklistOemAck) {
        PermissionsInspector.inspect(context, checklistOemAck)
    }
    DisposableEffect(lifecycleOwner) {
        val checklistObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(checklistObserver)
        onDispose { lifecycleOwner.lifecycle.removeObserver(checklistObserver) }
    }

    // ─── Permission launchers ─────────────────────────────────────────────────

    // Step 0 — POST_NOTIFICATIONS
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> stepIndex = 1 }

    // Step 1 — FINE_LOCATION + COARSE_LOCATION
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> stepIndex = 2 }

    // ─── ON_RESUME observer for steps 2/3 (external settings screens) ────────

    if (isWaitingForResume) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isWaitingForResume = false
                    stepIndex++
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    // ─── Ladder step driver ──────────────────────────────────────────────────

    LaunchedEffect(stepIndex) {
        if (stepIndex < 0 || isWaitingForResume) return@LaunchedEffect
        val status = PermissionLadder.checkStatus(context, oemGuidanceShown)
        when (stepIndex) {
            0 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !status.notificationsGranted) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    stepIndex = 1
                }
            }
            1 -> {
                if (!status.fineLocationGranted) {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    )
                } else {
                    stepIndex = 2
                }
            }
            2 -> {
                if (!status.backgroundLocationGranted) {
                    PermissionLadder.launchLocationSettings(context)
                    isWaitingForResume = true
                } else {
                    stepIndex = 3
                }
            }
            3 -> {
                if (!status.batteryOptExempt) {
                    PermissionLadder.launchBatteryOptimizationRequest(context)
                    isWaitingForResume = true
                } else {
                    stepIndex = 4
                }
            }
            4 -> {
                // OEM guidance — advisory. Mark shown and advance immediately.
                // The dialog renders the guidance UI while stepIndex == 4 (before this effect fires).
                oemGuidanceShown = true
                stepIndex = 5
            }
            5 -> {
                // Final evaluation
                stepIndex = -1  // ladder done
                val finalStatus = PermissionLadder.checkStatus(context, oemGuidanceShown)
                // P2: only the MINIMUM (notifications + precise location) is required to start.
                // Background "Allow all the time" + battery exemption are recommended (advisory),
                // not blocking — so granting them or not, the engine still starts.
                if (finalStatus.minimumToStartGranted) {
                    showPermissionsNotice = false
                    onPermissionsGranted()
                } else {
                    showPermissionsNotice = true
                    onPermissionsDenied()
                }
            }
        }
    }

    // ─── UI ──────────────────────────────────────────────────────────────────

    val isLadderRunning = stepIndex >= 0

    DialogScaffold(onDismiss = onDismiss) {

        // Title
        Text(
            text = t("autoActivities.title", null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )

        HorizontalDivider(color = CheckingDivider)

        // Explanation
        Text(
            text = t("autoActivities.explanation", null),
            style = MaterialTheme.typography.bodyMedium,
            color = CheckingTextMuted,
        )

        HorizontalDivider(color = CheckingDivider)

        // Checkbox — disabled while the ladder runs, or when location permission is
        // insufficient (precise + "Allow all the time"). Insufficient → forced unchecked.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                // Reflects the user's INTENT to enable, independent of whether the permissions are
                // already granted. Removes the old deadlock (box stayed disabled until permissions
                // existed, but permissions could only be granted through this same dialog).
                checked = automaticActivitiesEnabled,
                onCheckedChange = { checked ->
                    showPermissionsNotice = false
                    onToggleChanged(checked)
                    // Turning ON without sufficient permissions launches the ladder to grant them;
                    // the ViewModel starts the FGS once granted (onAutoActivitiesPermissionsGranted).
                    if (checked && !permissionsSufficient) {
                        stepIndex = 0
                    }
                },
                enabled = !isLadderRunning,
                colors = CheckboxDefaults.colors(checkedColor = CheckingPrimary),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = t("autoActivities.enable", null),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingTextStrong,
            )
        }

        // P4.1 — live permission checklist: tap a row to fix it; status refreshes on return (ON_RESUME).
        HorizontalDivider(color = CheckingDivider)
        ChecklistRow(
            label = t("autoActivities.permNotifications", null),
            statusText = if (permStatus.notificationsGranted) t("permissions.notificationsAllowed", null)
            else t("permissions.notificationsDisallowed", null),
            tone = if (permStatus.notificationsGranted) ToneGood else ToneBad,
            onFix = { PermissionLadder.launchAppNotificationSettings(context) },
        )
        val (locationStatusText, locationTone) = when {
            permStatus.location == LocationStatus.PRECISE && permStatus.backgroundGranted ->
                t("permissions.locationPrecise", null) to ToneGood
            permStatus.location == LocationStatus.PRECISE ->
                t("permissions.locationPreciseNoBackground", null) to ToneWarn
            permStatus.location == LocationStatus.IMPRECISE ->
                t("permissions.locationImprecise", null) to ToneWarn
            else ->
                t("permissions.locationDenied", null) to ToneBad
        }
        ChecklistRow(
            label = t("autoActivities.permLocationAllTime", null),
            statusText = locationStatusText,
            tone = locationTone,
            onFix = { PermissionLadder.launchLocationSettings(context) },
        )
        ChecklistRow(
            label = t("autoActivities.permBattery", null),
            statusText = if (permStatus.batteryRestricted) t("permissions.batteryRestricted", null)
            else t("permissions.batteryUnrestricted", null),
            tone = if (permStatus.batteryRestricted) ToneBad else ToneGood,
            onFix = { PermissionLadder.launchBatteryOptimizationRequest(context) },
        )
        if (PermissionLadder.detectOemType() != OemType.GENERIC) {
            ChecklistRow(
                label = t("autoActivities.permAutoStart", null),
                statusText = if (permStatus.autoStartEnabled) t("permissions.autoStartOn", null)
                else t("permissions.autoStartOff", null),
                tone = if (permStatus.autoStartEnabled) ToneGood else ToneBad,
                onFix = {
                    checklistOemAck = true
                    PermissionLadder.launchOemAutostartSettings(context)
                },
            )
        }

        // Insufficient-permissions notice (BLOCKING) — shown when the user wants the feature on
        // (intent) but the MINIMUM (notifications + precise location) is still missing.
        if (automaticActivitiesEnabled && !permissionsSufficient) {
            Text(
                text = t("autoActivities.insufficientPermissions", null),
                style = MaterialTheme.typography.bodySmall,
                color = CheckingError,
            )
        }

        // Reduced-reliability notice (ADVISORY) — minimum granted (engine runs) but the recommended
        // permissions (background "Allow all the time" + battery exemption) are still missing.
        if (automaticActivitiesEnabled && permissionsSufficient &&
            !PermissionLadder.checkStatus(context).allRecommendedGranted
        ) {
            Text(
                text = t("autoActivities.reducedReliability", null),
                style = MaterialTheme.typography.bodySmall,
                color = CheckingTextMuted,
            )
        }

        // "Revisar Permissões" — shown when the feature is enabled (intent on), so the user can
        // (re)grant any missing permission. Checking the box already auto-launches the ladder.
        if (automaticActivitiesEnabled) {
            SecondaryButton(
                text = t("autoActivities.reviewPermissions", null),
                onClick = {
                    showPermissionsNotice = false
                    stepIndex = 0
                },
                enabled = !isLadderRunning,
            )

            // Denial notice — shown directly below the button after a failed run
            if (showPermissionsNotice) {
                Text(
                    text = t("autoActivities.permissionsNotice", null),
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingError,
                )
            }

            // OEM guidance — shown while step 4 is pending (step index == 4 and not yet advanced)
            val oem = PermissionLadder.detectOemType()
            if (isLadderRunning && stepIndex == 4 && oem != OemType.GENERIC && !oemGuidanceShown) {
                HorizontalDivider(color = CheckingDivider)
                Text(
                    text = t("autoActivities.permStep.oemGuidanceTitle", null),
                    style = MaterialTheme.typography.labelMedium,
                    color = CheckingTextStrong,
                )
                Text(
                    text = t("autoActivities.permStep.oemGuidanceBody", null),
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
                if (PermissionLadder.canDeepLinkToOemAutostart(context)) {
                    PrimaryButton(
                        text = t("autoActivities.reviewPermissions", null),
                        onClick = { PermissionLadder.launchOemAutostartSettings(context) },
                    )
                }
            }
        }

        HorizontalDivider(color = CheckingDivider)

        // "Fechar" button
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = t("autoActivities.close", null),
                color = CheckingPrimary,
            )
        }
    }
}

// P4.1 tone palette (inherited from the former PermissionsDialog): green / orange / red for the checklist statuses.
private val ToneGood = CheckingLatestBorder
private val ToneWarn = Color(0xFFEA580C)
private val ToneBad = CheckingError

/** A tappable permission row: label + colored status; the whole row launches the relevant fix screen. */
@Composable
private fun ChecklistRow(
    label: String,
    statusText: String,
    tone: Color,
    onFix: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFix)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CheckingTextStrong,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = tone,
        )
    }
}
