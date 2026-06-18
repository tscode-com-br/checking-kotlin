package br.com.tscode.checking.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.tscode.checking.i18n.t
import br.com.tscode.checking.presentation.components.AutoActivitiesNudgeCard
import br.com.tscode.checking.presentation.theme.CheckingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// P5.2 — smoke tests for AutoActivitiesNudgeCard: renders the question + both actions, and each
// button fires its callback.
class AutoActivitiesNudgeCardSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun tPt(key: String) = t(key, null, "pt")

    @Test
    fun nudgeCard_rendersQuestionAndBothActions() {
        composeRule.setContent {
            CheckingTheme {
                AutoActivitiesNudgeCard(
                    onActivate = {},
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText(tPt("autoActivities.nudgeQuestion")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("autoActivities.nudgeActivate")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("autoActivities.nudgeLater")).assertIsDisplayed()
    }

    @Test
    fun nudgeCard_activateButton_firesCallback() {
        var activated = false
        composeRule.setContent {
            CheckingTheme {
                AutoActivitiesNudgeCard(
                    onActivate = { activated = true },
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText(tPt("autoActivities.nudgeActivate")).performClick()
        assertTrue(activated)
    }

    @Test
    fun nudgeCard_laterButton_firesDismiss() {
        var dismissed = false
        composeRule.setContent {
            CheckingTheme {
                AutoActivitiesNudgeCard(
                    onActivate = {},
                    onDismiss = { dismissed = true },
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText(tPt("autoActivities.nudgeLater")).performClick()
        assertTrue(dismissed)
    }
}
