package br.com.tscode.checking.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.CheckHistoryEntry
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.i18n.t
import br.com.tscode.checking.presentation.components.CheckHistoryDialog
import br.com.tscode.checking.presentation.theme.CheckingTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

// TP6 — smoke tests for CheckHistoryDialog (change D): renders the Data/Hora/Local table, the empty
// state, and "-" for a null location.
class CheckHistoryDialogSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun tPt(key: String) = t(key, null, "pt")

    private fun entry(local: String?) = CheckHistoryEntry(
        action = CheckAction.CHECKIN,
        projeto = "P80",
        local = local,
        time = Instant.parse("2026-06-15T01:00:00Z"),
        informe = InformeType.NORMAL,
    )

    @Test
    fun historyDialog_rendersTitleHeadersAndLocation() {
        composeRule.setContent {
            CheckingTheme {
                CheckHistoryDialog(
                    action = CheckAction.CHECKIN,
                    entries = listOf(entry("Área X")),
                    isLoading = false,
                    isError = false,
                    onRetry = {},
                    langCode = "pt",
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }
        composeRule.onNodeWithText(tPt("history.dialogTitleCheckin")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("history.colDate")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("history.colTime")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("history.colLocal")).assertIsDisplayed()
        composeRule.onNodeWithText("Área X").assertIsDisplayed()
    }

    @Test
    fun historyDialog_nullLocal_rendersDash() {
        composeRule.setContent {
            CheckingTheme {
                CheckHistoryDialog(
                    action = CheckAction.CHECKIN,
                    entries = listOf(entry(null)),
                    isLoading = false,
                    isError = false,
                    onRetry = {},
                    langCode = "pt",
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }
        composeRule.onNodeWithText("-").assertIsDisplayed() // null local → "-"
    }

    @Test
    fun historyDialog_emptyState() {
        composeRule.setContent {
            CheckingTheme {
                CheckHistoryDialog(
                    action = CheckAction.CHECKOUT,
                    entries = emptyList(),
                    isLoading = false,
                    isError = false,
                    onRetry = {},
                    langCode = "pt",
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }
        composeRule.onNodeWithText(tPt("history.empty")).assertIsDisplayed()
    }

    // TP1.3 — the error+retry state is distinct from "empty": a load failure shows the error message + a
    // retry affordance and NOT the empty-history message.
    @Test
    fun historyDialog_errorState_showsErrorAndRetry_notEmpty() {
        composeRule.setContent {
            CheckingTheme {
                CheckHistoryDialog(
                    action = CheckAction.CHECKIN,
                    entries = emptyList(),
                    isLoading = false,
                    isError = true,
                    onRetry = {},
                    langCode = "pt",
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }
        composeRule.onNodeWithText(tPt("history.loadError")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("history.retry")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("history.empty")).assertDoesNotExist() // error shadows empty
    }

    // TP1.3 — the Atividade column header + the per-action activity label render (CHECKIN row).
    @Test
    fun historyDialog_activityColumn_checkin_showsCheckinLabel() {
        composeRule.setContent {
            CheckingTheme {
                CheckHistoryDialog(
                    action = CheckAction.CHECKIN,
                    entries = listOf(entry("Área X")),
                    isLoading = false,
                    isError = false,
                    onRetry = {},
                    langCode = "pt",
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }
        composeRule.onNodeWithText(tPt("history.colActivity")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("history.activityCheckin")).assertIsDisplayed()
    }

    // TP1.3 — the per-action activity label for a CHECKOUT row.
    @Test
    fun historyDialog_activityColumn_checkout_showsCheckoutLabel() {
        composeRule.setContent {
            CheckingTheme {
                CheckHistoryDialog(
                    action = CheckAction.CHECKOUT,
                    entries = listOf(
                        CheckHistoryEntry(
                            action = CheckAction.CHECKOUT,
                            projeto = "P80",
                            local = "Gate 3",
                            time = Instant.parse("2026-06-15T03:00:00Z"),
                            informe = InformeType.NORMAL,
                        ),
                    ),
                    isLoading = false,
                    isError = false,
                    onRetry = {},
                    langCode = "pt",
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }
        composeRule.onNodeWithText(tPt("history.activityCheckout")).assertIsDisplayed()
    }
}
