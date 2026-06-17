package br.com.tscode.checking.presentation.settings.scheduledpause

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.components.DialogScaffold
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

// "Pausa Programada" dialog — §23.7.1, T3B.10.
// Opened from Settings → "Pausa Programada" when authenticated.
// All edits call onSettingChanged immediately so they persist on every toggle/time change.
// Time selection uses the same Material3 TimePicker (dial + "Voltar para a hora") as the
// Transporte Extra builder. Spacing is uniform — DialogScaffold spaces all children by
// sectionGap, so the gap between every checkbox is identical.
@Composable
fun ScheduledPauseDialog(
    scheduledPauseEnabled: Boolean,
    scheduledPauseFrom: String,
    scheduledPauseTo: String,
    suspendSaturdays: Boolean,
    suspendSundays: Boolean,
    onSettingChanged: (
        enabled: Boolean,
        from: String,
        to: String,
        suspendSat: Boolean,
        suspendSun: Boolean,
    ) -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    var showFromPicker by rememberSaveable { mutableStateOf(false) }
    var showToPicker by rememberSaveable { mutableStateOf(false) }

    if (showFromPicker) {
        TimePickerAlert(
            initial = scheduledPauseFrom,
            onConfirm = { picked ->
                onSettingChanged(scheduledPauseEnabled, picked, scheduledPauseTo, suspendSaturdays, suspendSundays)
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false },
            t = t,
        )
    }
    if (showToPicker) {
        TimePickerAlert(
            initial = scheduledPauseTo,
            onConfirm = { picked ->
                onSettingChanged(scheduledPauseEnabled, scheduledPauseFrom, picked, suspendSaturdays, suspendSundays)
                showToPicker = false
            },
            onDismiss = { showToPicker = false },
            t = t,
        )
    }

    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = t("scheduledPause.title", null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )

        Text(
            text = t("scheduledPause.explanation", null),
            style = MaterialTheme.typography.bodySmall,
            color = CheckingTextMuted,
        )

        // Checkbox: Ativar pausa programada.
        CheckboxRow(
            checked = scheduledPauseEnabled,
            label = t("scheduledPause.enable", null),
            onToggle = { checked ->
                onSettingChanged(checked, scheduledPauseFrom, scheduledPauseTo, suspendSaturdays, suspendSundays)
            },
        )

        // Time fields — only when enabled. Same field/picker model as Transporte Extra.
        if (scheduledPauseEnabled) {
            TimeField(
                label = t("scheduledPause.from", null),
                value = scheduledPauseFrom,
                onClick = { showFromPicker = true },
            )
            TimeField(
                label = t("scheduledPause.to", null),
                value = scheduledPauseTo,
                onClick = { showToPicker = true },
            )
        }

        // Checkbox: Suspender aos sábados.
        CheckboxRow(
            checked = suspendSaturdays,
            label = t("scheduledPause.suspendSaturdays", null),
            onToggle = { checked ->
                onSettingChanged(scheduledPauseEnabled, scheduledPauseFrom, scheduledPauseTo, checked, suspendSundays)
            },
        )

        // Checkbox: Suspender aos domingos.
        CheckboxRow(
            checked = suspendSundays,
            label = t("scheduledPause.suspendSundays", null),
            onToggle = { checked ->
                onSettingChanged(scheduledPauseEnabled, scheduledPauseFrom, scheduledPauseTo, suspendSaturdays, checked)
            },
        )

        SecondaryButton(
            text = t("scheduledPause.close", null),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun CheckboxRow(
    checked: Boolean,
    label: String,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(checkedColor = CheckingPrimary),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CheckingTextStrong,
        )
    }
}

// Read-only time field: bold label above a bordered, clickable box with a clock icon.
// Mirrors the Transporte Extra PickerField.
@Composable
private fun TimeField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(Tokens.controlRadius)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CheckingTextMuted,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Tokens.controlHeight)
                .background(CheckingCardBg, shape)
                .border(BorderStroke(1.dp, CheckingInputBorder), shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = CheckingTextMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = CheckingTextStrong,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerAlert(
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    val (initH, initM) = parseHhmm(initial)
    val timeState = rememberTimePickerState(initialHour = initH, initialMinute = initM, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm("%02d:%02d".format(timeState.hour, timeState.minute)) }) {
                Text(t("transport.requestBuilder.pickerConfirm", null))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(t("transport.requestBuilder.pickerCancel", null))
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
            ) {
                TimePicker(state = timeState)
                // "Voltar para a hora" — returns the dial to hour selection so the user
                // can fix a wrong hour without cancelling.
                SecondaryButton(
                    text = t("transport.requestBuilder.backToHour", null),
                    onClick = { timeState.selection = TimePickerSelectionMode.Hour },
                )
            }
        },
    )
}

private fun parseHhmm(hhmm: String): Pair<Int, Int> {
    val parts = hhmm.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 22
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return Pair(h, m)
}
