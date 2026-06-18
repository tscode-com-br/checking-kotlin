package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.CheckHistoryEntry
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HISTORY_ZONE = ZoneId.of("Asia/Singapore")

private fun historyLocale(langCode: String): Locale = when (langCode) {
    "pt" -> Locale.forLanguageTag("pt-BR")
    "zh" -> Locale.forLanguageTag("zh-CN")
    "ms" -> Locale.forLanguageTag("ms-MY")
    "id" -> Locale.forLanguageTag("id-ID")
    "tl" -> Locale.forLanguageTag("fil-PH")
    else -> Locale.ENGLISH
}

/**
 * P2.2 (change D) — full check-in OR check-out history as a Data/Hora/Local table.
 * Opened by tapping a [HistoryCard] cell; `action` selects which title to show. Date/time use the
 * same Asia/Singapore zone + locale formatting as [HistoryCard]; a null location renders as "-".
 */
@Composable
fun CheckHistoryDialog(
    action: CheckAction,
    entries: List<CheckHistoryEntry>,
    isLoading: Boolean,
    langCode: String,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    val titleKey = if (action == CheckAction.CHECKIN) "history.dialogTitleCheckin" else "history.dialogTitleCheckout"
    val locale = historyLocale(langCode)
    val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yy", locale)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", locale)

    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = t(titleKey, null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )

        when {
            isLoading -> Text(
                text = t("history.loadingMessage", null),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingTextMuted,
            )
            entries.isEmpty() -> Text(
                text = t("history.empty", null),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingTextMuted,
            )
            else -> {
                // Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    HeaderCell(t("history.colDate", null), weight = 1f)
                    HeaderCell(t("history.colTime", null), weight = 0.8f)
                    HeaderCell(t("history.colLocal", null), weight = 1.6f)
                }
                HorizontalDivider(color = CheckingDivider)
                entries.forEach { entry ->
                    val zoned = entry.time?.atZone(HISTORY_ZONE)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        BodyCell(zoned?.format(dateFmt) ?: "-", weight = 1f)
                        BodyCell(zoned?.format(timeFmt) ?: "-", weight = 0.8f)
                        BodyCell(entry.local ?: "-", weight = 1.6f)
                    }
                }
            }
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(text = t("history.back", null), color = CheckingPrimary)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = CheckingTextMuted,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BodyCell(text: String, weight: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = CheckingTextStrong,
        modifier = Modifier.weight(weight),
    )
}
