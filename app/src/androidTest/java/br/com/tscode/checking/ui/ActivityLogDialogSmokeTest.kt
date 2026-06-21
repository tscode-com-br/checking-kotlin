package br.com.tscode.checking.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivityLogEntry
import br.com.tscode.checking.domain.model.ActivitySeverity
import br.com.tscode.checking.presentation.settings.activitylog.ActivityLogDialog
import br.com.tscode.checking.presentation.theme.CheckingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Smoke tests for ActivityLogDialog (plan004 EP8). The table is ENGLISH-ONLY, day-grouped, severity-colored.
// Compile-gated; on-device run pending (colors are device-visual; see plan003 TP5 convention).
class ActivityLogDialogSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val day1 = Instant.parse("2026-06-19T10:15:00Z")
    private val day2 = Instant.parse("2026-06-20T08:30:00Z")

    // Mirror the dialog's exact formatters so expected header/time strings are deterministic on any device.
    private val dayFmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault())
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

    private fun entry(
        at: Instant,
        actor: ActivityActor = ActivityActor.SYS,
        kind: ActivityKind = ActivityKind.SYSTEM,
        sev: ActivitySeverity = ActivitySeverity.INFO,
        desc: String = "row",
    ) = ActivityLogEntry(at = at, actor = actor, kind = kind, severity = sev, description = desc, location = null)

    @Test
    fun activityLogDialog_emptyState_rendersMessage() {
        composeRule.setContent {
            CheckingTheme {
                ActivityLogDialog(
                    entries = emptyList(),
                    isLoading = false,
                    canLoadMore = false,
                    onLoadMore = {},
                    onClear = {},
                    onDismiss = {},
                )
            }
        }
        composeRule.onNodeWithText("Activities").assertIsDisplayed()
        composeRule.onNodeWithText("No activity recorded yet.").assertIsDisplayed()
    }

    @Test
    fun activityLogDialog_withEntries_rendersEnglishRows() {
        composeRule.setContent {
            CheckingTheme {
                ActivityLogDialog(
                    entries = listOf(
                        entry(day2, ActivityActor.USER, ActivityKind.CHECK_IN, ActivitySeverity.SUCCESS, "Check-in at Gate 3."),
                        entry(day1, ActivityActor.SYS, ActivityKind.SYSTEM, ActivitySeverity.WARNING, "Automatic activities are OFF."),
                    ),
                    isLoading = false,
                    canLoadMore = false,
                    onLoadMore = {},
                    onClear = {},
                    onDismiss = {},
                )
            }
        }
        // English-only Activity-column vocabulary + Who + descriptions render verbatim.
        composeRule.onNodeWithText("check-in").assertIsDisplayed()
        composeRule.onNodeWithText("user").assertIsDisplayed()
        composeRule.onNodeWithText("Check-in at Gate 3.").assertIsDisplayed()
        composeRule.onNodeWithText("Automatic activities are OFF.").assertIsDisplayed()
    }

    @Test
    fun activityLogDialog_closeButton_callsDismiss() {
        var dismissed = false
        composeRule.setContent {
            CheckingTheme {
                ActivityLogDialog(
                    entries = emptyList(),
                    isLoading = false,
                    canLoadMore = false,
                    onLoadMore = {},
                    onClear = {},
                    onDismiss = { dismissed = true },
                )
            }
        }
        composeRule.onNodeWithText("Close").performClick()
        assertTrue(dismissed)
    }

    // TP4 — entries spanning two local days render TWO date headers (newest day's group first) + the
    // HH:mm:ss time column. Instants are 2 days apart → different local days in any timezone.
    @Test
    fun entriesAcrossTwoDays_renderTwoDateHeaders_andTimeColumn() {
        val d1 = Instant.parse("2026-06-19T12:00:00Z")
        val d2 = Instant.parse("2026-06-21T12:00:00Z")
        composeRule.setContent {
            CheckingTheme {
                ActivityLogDialog(
                    entries = listOf(
                        entry(d2, ActivityActor.SYS, ActivityKind.ACTIVE, ActivitySeverity.INFO, "Checking is now active."),
                        entry(d1, ActivityActor.USER, ActivityKind.CHECK_IN, ActivitySeverity.SUCCESS, "Check-in at Gate 3."),
                    ),
                    isLoading = false, canLoadMore = false, onLoadMore = {}, onClear = {}, onDismiss = {},
                )
            }
        }
        composeRule.onNodeWithText(dayFmt.format(d2)).assertIsDisplayed()  // newest day's header
        composeRule.onNodeWithText(dayFmt.format(d1)).assertIsDisplayed()  // older day's header
        composeRule.onNodeWithText(timeFmt.format(d2)).assertIsDisplayed() // Time column (HH:mm:ss)
    }

    // TP4 — every severity renders its row. The actual COLOR (green/red/orange/dark-blue) is device-visual,
    // asserted on-device per the plan003 TP5 convention; here we prove each severity renders without crashing.
    @Test
    fun allFourSeverities_render() {
        val base = Instant.parse("2026-06-20T09:00:00Z")
        composeRule.setContent {
            CheckingTheme {
                ActivityLogDialog(
                    entries = listOf(
                        entry(base.plusSeconds(3), ActivityActor.USER, ActivityKind.CHECK_IN, ActivitySeverity.SUCCESS, "Check-in at Gate 3."),
                        entry(base.plusSeconds(2), ActivityActor.SYS, ActivityKind.CHECK_OUT, ActivitySeverity.FAILURE, "Check-out failed at Gate 3."),
                        entry(base.plusSeconds(1), ActivityActor.SYS, ActivityKind.SYNC, ActivitySeverity.WARNING, "Check-in queued (offline) at Gate 3."),
                        entry(base, ActivityActor.SYS, ActivityKind.SYSTEM, ActivitySeverity.INFO, "App started."),
                    ),
                    isLoading = false, canLoadMore = false, onLoadMore = {}, onClear = {}, onDismiss = {},
                )
            }
        }
        composeRule.onNodeWithText("Check-in at Gate 3.").assertIsDisplayed()          // SUCCESS → green
        composeRule.onNodeWithText("Check-out failed at Gate 3.").assertIsDisplayed()   // FAILURE → red
        composeRule.onNodeWithText("Check-in queued (offline) at Gate 3.").assertIsDisplayed() // WARNING → orange
        composeRule.onNodeWithText("App started.").assertIsDisplayed()                  // INFO → dark blue
    }

    // TP4 — reaching the end of the list while more pages exist requests the next block (the derivedStateOf
    // near-end → LaunchedEffect → onLoadMore path). Rows all fit, so "near end" holds without a manual scroll
    // (the physical scroll-past-30 gesture is device-visual; this proves the trigger logic).
    @Test
    fun nearingEnd_withMorePages_triggersLoadMore() {
        var loadMoreCalled = false
        val base = Instant.parse("2026-06-20T09:00:00Z")
        composeRule.setContent {
            CheckingTheme {
                ActivityLogDialog(
                    entries = (1..5).map { entry(base.plusSeconds(it.toLong()), desc = "r$it") },
                    isLoading = false,
                    canLoadMore = true,
                    onLoadMore = { loadMoreCalled = true },
                    onClear = {}, onDismiss = {},
                )
            }
        }
        composeRule.waitForIdle()
        assertTrue("reaching the end with canLoadMore must trigger loadMore", loadMoreCalled)
    }

    // TP4 — the Clear action invokes onClear.
    @Test
    fun clearButton_callsOnClear() {
        var cleared = false
        composeRule.setContent {
            CheckingTheme {
                ActivityLogDialog(
                    entries = listOf(entry(Instant.parse("2026-06-20T09:00:00Z"), desc = "x")),
                    isLoading = false, canLoadMore = false, onLoadMore = {},
                    onClear = { cleared = true }, onDismiss = {},
                )
            }
        }
        composeRule.onNodeWithText("Clear").performClick()
        assertTrue(cleared)
    }
}
