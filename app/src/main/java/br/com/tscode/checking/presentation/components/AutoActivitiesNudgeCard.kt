package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

/**
 * P5.2 — one-time, dismissible first-login nudge inviting the user to enable automatic activities.
 *
 * Transient by design: the caller renders it only when `showAutoActivitiesNudge` is true, so it
 * contributes zero height to the main screen when absent. Visual style mirrors the other cards via
 * [TintedPanel] (slate tint + subtle teal border + rounded corners).
 *
 * @param onActivate primary action — opens the Auto-activities dialog (also hides the card via state).
 * @param onDismiss low-emphasis action — dismisses the nudge forever for this chave.
 */
@Composable
fun AutoActivitiesNudgeCard(
    onActivate: () -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
    modifier: Modifier = Modifier,
) {
    TintedPanel(modifier = modifier) {
        Text(
            text = t("autoActivities.nudgeQuestion", null),
            style = MaterialTheme.typography.bodyMedium,
            color = CheckingTextStrong,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = t("autoActivities.nudgeLater", null),
                    style = MaterialTheme.typography.labelLarge,
                    color = CheckingTextMuted,
                )
            }
            PrimaryButton(
                text = t("autoActivities.nudgeActivate", null),
                onClick = onActivate,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
