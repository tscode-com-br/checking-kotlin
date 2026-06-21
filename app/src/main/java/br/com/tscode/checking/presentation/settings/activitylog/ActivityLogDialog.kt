package br.com.tscode.checking.presentation.settings.activitylog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivityLogEntry
import br.com.tscode.checking.domain.model.ActivitySeverity
import br.com.tscode.checking.presentation.components.DialogScaffold
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingActivityInfo
import br.com.tscode.checking.presentation.theme.CheckingActivityWarning
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingErrorVivid
import br.com.tscode.checking.presentation.theme.CheckingSuccess
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * plan004 §3.5 (EP8) — read-only "Activities" debug log viewer. **ENGLISH-ONLY by design**: every string
 * here (title, Who/Activity text, descriptions, date headers, buttons) is a hardcoded English literal —
 * only the Settings *row label* that opens this dialog is localized. Style mirrors [EvaluationLogDialog]:
 * a [DialogScaffold] whose body is a bounded-height [LazyColumn] (it sits inside the scaffold's own
 * verticalScroll, so the list must NOT be unbounded). Entries arrive newest-first, paged in blocks of 30,
 * grouped under a date header per local day (newest day & row first). Row color comes from `severity` only.
 */
@Composable
fun ActivityLogDialog(
    entries: List<ActivityLogEntry>,
    isLoading: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val zone = remember { ZoneId.systemDefault() }
    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm:ss").withZone(zone) }
    val dayFmt = remember { DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH).withZone(zone) }

    // Lazy "load more" on scroll: when the last visible row nears the end of the loaded list and more
    // pages exist, ask the VM for the next block. The VM guards re-entrancy, so a double-fire is harmless.
    val listState = rememberLazyListState()
    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && last >= total - 3
        }
    }
    LaunchedEffect(nearEnd, canLoadMore, isLoading) {
        if (nearEnd && canLoadMore && !isLoading) onLoadMore()
    }

    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = "Activities",
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )
        HorizontalDivider(color = CheckingDivider)

        if (entries.isEmpty() && !isLoading) {
            Text(
                text = "No activity recorded yet.",
                style = MaterialTheme.typography.bodySmall,
                color = CheckingTextMuted,
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().height(440.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(entries) { index, entry ->
                    // Emit a date header whenever the local day changes from the previous (older) row.
                    val prev = entries.getOrNull(index - 1)
                    val showHeader = prev == null ||
                        entry.at.atZone(zone).toLocalDate() != prev.at.atZone(zone).toLocalDate()
                    if (showHeader) {
                        if (index != 0) Spacer(Modifier.height(4.dp))
                        Text(
                            text = dayFmt.format(entry.at),
                            style = MaterialTheme.typography.labelMedium,
                            color = CheckingTextMuted,
                        )
                    }
                    ActivityRow(entry, timeFmt)
                    HorizontalDivider(color = CheckingDivider)
                }
                if (isLoading) {
                    item {
                        Text(
                            text = "Loading…",
                            style = MaterialTheme.typography.labelSmall,
                            color = CheckingTextMuted,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onClear) {
                Text(text = "Clear", color = CheckingTextMuted)
            }
            Spacer(Modifier.weight(1f))
            SecondaryButton(text = "Close", onClick = onDismiss)
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityLogEntry, timeFmt: DateTimeFormatter) {
    val color = severityColor(entry.severity)
    val who = if (entry.actor == ActivityActor.USER) "user" else "sys"
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Time (device zone) · Who · Activity — the Activity text is tinted by severity.
            Text(
                text = timeFmt.format(entry.at),
                style = MaterialTheme.typography.labelSmall,
                color = CheckingTextMuted,
                fontFamily = FontFamily.Monospace,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = who,
                    style = MaterialTheme.typography.labelSmall,
                    color = CheckingTextMuted,
                )
                Text(
                    text = kindText(entry.kind),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
            }
        }
        Text(
            text = entry.description,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

// Color comes from severity ONLY (plan004 §3.1): SUCCESS=green, FAILURE=red, WARNING=orange, INFO=dark blue.
private fun severityColor(severity: ActivitySeverity): Color = when (severity) {
    ActivitySeverity.SUCCESS -> CheckingSuccess
    ActivitySeverity.FAILURE -> CheckingErrorVivid
    ActivitySeverity.WARNING -> CheckingActivityWarning
    ActivitySeverity.INFO -> CheckingActivityInfo
}

// English-only "Activity" column text — the controlled vocabulary (plan004 §3.2b).
private fun kindText(kind: ActivityKind): String = when (kind) {
    ActivityKind.CHECK_IN -> "check-in"
    ActivityKind.CHECK_OUT -> "check-out"
    ActivityKind.ACTIVE -> "active"
    ActivityKind.INACTIVE -> "inactive"
    ActivityKind.TRIGGER -> "trigger"
    ActivityKind.LOCATION -> "location"
    ActivityKind.SYNC -> "sync"
    ActivityKind.AUTH -> "auth"
    ActivityKind.SYSTEM -> "system"
    ActivityKind.ERROR -> "error"
}
