package br.com.tscode.checking.presentation.settings.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.components.DialogScaffold
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

/**
 * "Notificações" dialog. Three opt-in checkboxes for push notifications. Each change is
 * persisted immediately (via onChanged), so "Voltar" just closes the dialog.
 */
@Composable
fun NotificationsDialog(
    notifyActivities: Boolean,
    notifyScheduledPause: Boolean,
    notifyAccident: Boolean,
    onChanged: (activities: Boolean, scheduledPause: Boolean, accident: Boolean) -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = t("notifications.title", null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )
        HorizontalDivider(color = CheckingDivider)

        Text(
            text = t("notifications.intro", null),
            style = MaterialTheme.typography.bodySmall,
            color = CheckingTextMuted,
        )

        CheckboxRow(
            checked = notifyActivities,
            label = t("notifications.checkboxActivities", null),
            onToggle = { onChanged(it, notifyScheduledPause, notifyAccident) },
        )
        CheckboxRow(
            checked = notifyScheduledPause,
            label = t("notifications.checkboxScheduledPause", null),
            onToggle = { onChanged(notifyActivities, it, notifyAccident) },
        )
        CheckboxRow(
            checked = notifyAccident,
            label = t("notifications.checkboxAccident", null),
            onToggle = { onChanged(notifyActivities, notifyScheduledPause, it) },
        )

        Spacer(modifier = Modifier.height(8.dp))
        SecondaryButton(
            text = t("notifications.backButton", null),
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
