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
                    langCode = "pt",
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }
        composeRule.onNodeWithText(tPt("history.empty")).assertIsDisplayed()
    }
}
