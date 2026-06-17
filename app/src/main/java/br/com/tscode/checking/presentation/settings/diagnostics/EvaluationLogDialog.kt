package br.com.tscode.checking.presentation.settings.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.platform.background.OrchestratorTrigger
import br.com.tscode.checking.platform.background.diagnostics.EvaluationEntry
import br.com.tscode.checking.platform.background.diagnostics.EvaluationLog
import br.com.tscode.checking.platform.background.diagnostics.EvaluationOutcome
import br.com.tscode.checking.platform.location.LocationMeasurementCollector
import br.com.tscode.checking.platform.location.LocationMeasurementSummary
import br.com.tscode.checking.presentation.components.DialogScaffold
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Hidden debug screen — last-N orchestrator evaluations (§23.10, T3B.11).
// Only reachable via the debug-only "Diagnóstico" button in SettingsDialog.
@Composable
fun EvaluationLogDialog(onDismiss: () -> Unit) {
    val entries = remember { EvaluationLog.snapshot() }
    val locationSummary = remember {
        LocationMeasurementCollector.logSnapshot()   // console snapshot on open
        LocationMeasurementCollector.summarize()
    }

    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = "Evaluation Log (last ${entries.size})",
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )

        // Location telemetry summary (T7.1)
        if (locationSummary.isNotEmpty()) {
            HorizontalDivider(color = CheckingDivider)
            Text(
                text = "GPS Accuracy",
                style = MaterialTheme.typography.labelMedium,
                color = CheckingTextMuted,
            )
            locationSummary.forEach { (trigger, s) ->
                LocationSummaryRow(trigger, s)
            }
        }

        HorizontalDivider(color = CheckingDivider)

        if (entries.isEmpty()) {
            Text(
                text = "No evaluations recorded yet.",
                style = MaterialTheme.typography.bodySmall,
                color = CheckingTextMuted,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(entries) { entry ->
                    EvaluationEntryRow(entry)
                    HorizontalDivider(color = CheckingDivider)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        SecondaryButton(text = "Fechar", onClick = onDismiss)
    }
}

@Composable
private fun LocationSummaryRow(trigger: OrchestratorTrigger, s: LocationMeasurementSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = trigger.name,
            style = MaterialTheme.typography.labelSmall,
            color = CheckingTextStrong,
        )
        Text(
            text = "n=${s.count}  " +
                "min=${"%.0f".format(s.minMeters)}m  " +
                "med=${"%.0f".format(s.medianMeters)}m  " +
                "max=${"%.0f".format(s.maxMeters)}m",
            style = MaterialTheme.typography.labelSmall,
            color = CheckingTextMuted,
        )
    }
}

@Composable
private fun EvaluationEntryRow(entry: EvaluationEntry) {
    val fmt = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    val outcomeColor = when (entry.outcome) {
        EvaluationOutcome.SUBMITTED -> CheckingPrimary
        EvaluationOutcome.NO_ACTION -> CheckingTextMuted
        EvaluationOutcome.SKIP -> CheckingTextMuted
        EvaluationOutcome.PAUSED -> CheckingTextMuted
        EvaluationOutcome.NETWORK_ERROR -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
        EvaluationOutcome.TOGGLE_OFF -> CheckingTextMuted
    }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = fmt.format(entry.at),
                style = MaterialTheme.typography.labelSmall,
                color = CheckingTextMuted,
            )
            Text(
                text = "${entry.trigger.name} → ${entry.outcome.name}",
                style = MaterialTheme.typography.labelSmall,
                color = outcomeColor,
            )
        }
        if (entry.decidedAction != null || entry.resolvedLocal != null || entry.accuracyMeters != null) {
            val detail = buildList {
                entry.decidedAction?.let { add(it) }
                entry.resolvedLocal?.let { add(it) }
                entry.accuracyMeters?.let { add("±${"%.0f".format(it)}m") }
            }.joinToString(" · ")
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = CheckingTextStrong,
            )
        }
    }
}
