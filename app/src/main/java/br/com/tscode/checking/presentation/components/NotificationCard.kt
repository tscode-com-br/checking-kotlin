package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.check.NotificationTone
import br.com.tscode.checking.presentation.theme.CheckingErrorVivid
import br.com.tscode.checking.presentation.theme.CheckingSuccess
import br.com.tscode.checking.presentation.theme.CheckingTeal
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

@Composable
fun NotificationCard(
    primary: String,
    secondary: String,
    tone: NotificationTone,
    modifier: Modifier = Modifier,
) {
    if (primary.isEmpty() && secondary.isEmpty()) return

    // Maps to the web `.notification-line.is-*` colors.
    val primaryColor = when (tone) {
        NotificationTone.Error -> CheckingErrorVivid
        NotificationTone.Success -> CheckingSuccess
        NotificationTone.Teal -> CheckingTeal
        NotificationTone.Info -> CheckingTeal
        NotificationTone.None -> CheckingTextStrong
    }

    // The web `.notification-card` always reserves two lines (grid-template-rows:
    // repeat(2, …)). Render both slots, each with a reserved minimum height, so the
    // bar is always two lines tall even when the secondary line is empty.
    TintedPanel(modifier = modifier) {
        Text(
            text = primary,
            style = MaterialTheme.typography.labelLarge,
            color = primaryColor,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 18.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = secondary,
            style = MaterialTheme.typography.bodySmall,
            color = CheckingTextMuted,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 16.dp),
        )
    }
}
