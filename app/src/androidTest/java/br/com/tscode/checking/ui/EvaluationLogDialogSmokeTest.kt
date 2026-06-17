package br.com.tscode.checking.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.tscode.checking.platform.background.diagnostics.EvaluationEntry
import br.com.tscode.checking.platform.background.diagnostics.EvaluationLog
import br.com.tscode.checking.platform.background.diagnostics.EvaluationOutcome
import br.com.tscode.checking.platform.background.OrchestratorTrigger
import br.com.tscode.checking.presentation.settings.diagnostics.EvaluationLogDialog
import br.com.tscode.checking.presentation.theme.CheckingTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

// Smoke tests for EvaluationLogDialog composable (T7.3).
class EvaluationLogDialogSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun clearLog() {
        // The ring buffer is shared state; snapshot on each test is enough since we
        // only read — we don't need to clear between tests. The header text is deterministic.
    }

    @Test
    fun evaluationLogDialog_emptyState_rendersNoEntriesMessage() {
        // Snapshot whatever is in the ring buffer at the time (may be empty from a fresh run)
        val countAtTest = EvaluationLog.snapshot().size
        composeRule.setContent {
            CheckingTheme {
                EvaluationLogDialog(onDismiss = {})
            }
        }

        composeRule.onNodeWithText("Evaluation Log (last $countAtTest)").assertIsDisplayed()
        if (countAtTest == 0) {
            composeRule.onNodeWithText("No evaluations recorded yet.").assertIsDisplayed()
        }
    }

    @Test
    fun evaluationLogDialog_withEntry_rendersEntryRow() {
        EvaluationLog.record(
            EvaluationEntry(
                at = Instant.now(),
                trigger = OrchestratorTrigger.TIMER,
                accuracyMeters = 12.5,
                resolvedLocal = "Local A",
                decidedAction = "CHECKIN",
                outcome = EvaluationOutcome.SUBMITTED,
            )
        )

        composeRule.setContent {
            CheckingTheme {
                EvaluationLogDialog(onDismiss = {})
            }
        }

        // The trigger→outcome row should be visible
        composeRule.onNodeWithText("TIMER → SUBMITTED", substring = true).assertIsDisplayed()
    }

    @Test
    fun evaluationLogDialog_closeButton_callsDismiss() {
        var dismissed = false
        composeRule.setContent {
            CheckingTheme {
                EvaluationLogDialog(onDismiss = { dismissed = true })
            }
        }

        composeRule.onNodeWithText("Fechar").performClick()
        assertTrue(dismissed)
    }
}
