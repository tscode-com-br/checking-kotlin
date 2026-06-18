package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.presentation.theme.CheckingLatestBg
import br.com.tscode.checking.presentation.theme.CheckingLatestBorder
import br.com.tscode.checking.presentation.theme.CheckingTextMutedLight
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ZONE = ZoneId.of("Asia/Singapore")

private fun localeFor(langCode: String): Locale = when (langCode) {
    "pt" -> Locale.forLanguageTag("pt-BR")
    "zh" -> Locale.forLanguageTag("zh-CN")
    "ms" -> Locale.forLanguageTag("ms-MY")
    "id" -> Locale.forLanguageTag("id-ID")
    "tl" -> Locale.forLanguageTag("fil-PH")
    else -> Locale.ENGLISH
}

private enum class Latest { Checkin, Checkout, None }

@Composable
fun HistoryCard(
    historyState: HistoryState?,
    t: (String, Map<String, String>?) -> String,
    langCode: String,
    modifier: Modifier = Modifier,
    // P2.2 — tapping a cell opens the full history dialog (change D). Default no-op preserves the
    // pre-change look/behavior for any caller that doesn't wire them.
    onCheckinClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
) {
    val checkinAt = historyState?.lastCheckinAt
    val checkoutAt = historyState?.lastCheckoutAt
    val latest = when {
        checkinAt != null && checkoutAt != null ->
            if (checkinAt >= checkoutAt) Latest.Checkin else Latest.Checkout
        checkinAt != null -> Latest.Checkin
        checkoutAt != null -> Latest.Checkout
        else -> Latest.None
    }

    TintedPanel(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HistoryCell(
                label = t("history.lastCheckinLabel", null),
                instant = checkinAt,
                isLatest = latest == Latest.Checkin,
                langCode = langCode,
                t = t,
                onClick = onCheckinClick,
                modifier = Modifier.weight(1f),
            )
            HistoryCell(
                label = t("history.lastCheckoutLabel", null),
                instant = checkoutAt,
                isLatest = latest == Latest.Checkout,
                langCode = langCode,
                t = t,
                onClick = onCheckoutClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HistoryCell(
    label: String,
    instant: Instant?,
    isLatest: Boolean,
    langCode: String,
    t: (String, Map<String, String>?) -> String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(Tokens.controlRadius)
    val highlight = if (isLatest) {
        Modifier
            .clip(shape)
            .background(CheckingLatestBg)
            .border(BorderStroke(1.dp, CheckingLatestBorder), shape)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(shape)
            .then(highlight)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.uppercase(localeFor(langCode)) + ":",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = CheckingTextMutedLight,
            textAlign = TextAlign.Center,
        )
        if (instant != null) {
            val locale = localeFor(langCode)
            val zoned = instant.atZone(ZONE)
            // Day label: "Hoje"/"Ontem" for today/yesterday, otherwise the full
            // weekday name (e.g. "Sábado", "Segunda-feira") — never abbreviated.
            val date = zoned.toLocalDate()
            val today = LocalDate.now(ZONE)
            val dayLabel = when (date) {
                today -> t("history.today", null)
                today.minusDays(1) -> t("history.yesterday", null)
                else -> zoned.format(DateTimeFormatter.ofPattern("EEEE", locale))
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            }
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.bodySmall,
                color = CheckingTextStrong,
                textAlign = TextAlign.Center,
            )
            Text(
                text = zoned.format(DateTimeFormatter.ofPattern("dd/MM/yy", locale)),
                style = MaterialTheme.typography.titleSmall,
                color = CheckingTextStrong,
                textAlign = TextAlign.Center,
            )
            Text(
                text = zoned.format(DateTimeFormatter.ofPattern("HH:mm", locale)),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingTextStrong,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = "--",
                style = MaterialTheme.typography.titleSmall,
                color = CheckingTextStrong,
            )
        }
    }
}
