package br.com.tscode.checking.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.tscode.checking.i18n.t
import br.com.tscode.checking.presentation.components.SettingsDialog
import br.com.tscode.checking.presentation.theme.CheckingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Smoke tests for SettingsDialog composable (T7.3).
// Verifies: renders, key controls present, language list visible, auth-gated buttons.
class SettingsDialogSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun tPt(key: String) = t(key, null, "pt")

    @Test
    fun settingsDialog_renders_titleAndLanguageSection() {
        composeRule.setContent {
            CheckingTheme {
                SettingsDialog(
                    currentLanguage = "pt",
                    onLanguageSelected = {},
                    isAuthenticated = false,
                    hasPassword = false,
                    onResetPasswordClick = {},
                    onAutoActivitiesClick = {},
                    onScheduledPauseClick = {},
                    onPermissionsClick = {},
                    onNotificationsClick = {},
                    onSupportClick = {},
                    onAboutClick = {},
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText(tPt("settings.title")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("settings.languageLabel")).assertIsDisplayed()
    }

    @Test
    fun settingsDialog_showsAllSixLanguageOptions() {
        composeRule.setContent {
            CheckingTheme {
                SettingsDialog(
                    currentLanguage = "pt",
                    onLanguageSelected = {},
                    isAuthenticated = false,
                    hasPassword = false,
                    onResetPasswordClick = {},
                    onAutoActivitiesClick = {},
                    onScheduledPauseClick = {},
                    onPermissionsClick = {},
                    onNotificationsClick = {},
                    onSupportClick = {},
                    onAboutClick = {},
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        // Open the language dropdown; each non-current language then shows its native label.
        // ("Português" is the collapsed trigger label, so it appears twice once expanded —
        // assert the other five, which are unique.)
        composeRule.onNodeWithText("Português").performClick()
        listOf("English", "中文", "Bahasa Melayu", "Bahasa Indonesia", "Tagalog (Filipino)").forEach { label ->
            composeRule.onNodeWithText(label).assertIsDisplayed()
        }
    }

    @Test
    fun settingsDialog_languageSwitch_callsCallback() {
        var selectedLang = ""
        composeRule.setContent {
            CheckingTheme {
                SettingsDialog(
                    currentLanguage = "pt",
                    onLanguageSelected = { selectedLang = it },
                    isAuthenticated = false,
                    hasPassword = false,
                    onResetPasswordClick = {},
                    onAutoActivitiesClick = {},
                    onScheduledPauseClick = {},
                    onPermissionsClick = {},
                    onNotificationsClick = {},
                    onSupportClick = {},
                    onAboutClick = {},
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText("Português").performClick() // open the dropdown
        composeRule.onNodeWithText("English").performClick()
        assertTrue(selectedLang == "en")
    }

    @Test
    fun settingsDialog_whenAuthenticated_showsAutoActivitiesAndNightModeButtons() {
        composeRule.setContent {
            CheckingTheme {
                SettingsDialog(
                    currentLanguage = "pt",
                    onLanguageSelected = {},
                    isAuthenticated = true,
                    hasPassword = true,
                    onResetPasswordClick = {},
                    onAutoActivitiesClick = {},
                    onScheduledPauseClick = {},
                    onPermissionsClick = {},
                    onNotificationsClick = {},
                    onSupportClick = {},
                    onAboutClick = {},
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText(tPt("autoActivities.title")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("scheduledPause.buttonLabel")).assertIsDisplayed()
    }

    @Test
    fun settingsDialog_whenNotAuthenticated_hidesAutoActivitiesButtons() {
        composeRule.setContent {
            CheckingTheme {
                SettingsDialog(
                    currentLanguage = "pt",
                    onLanguageSelected = {},
                    isAuthenticated = false,
                    hasPassword = false,
                    onResetPasswordClick = {},
                    onAutoActivitiesClick = {},
                    onScheduledPauseClick = {},
                    onPermissionsClick = {},
                    onNotificationsClick = {},
                    onSupportClick = {},
                    onAboutClick = {},
                    onDismiss = {},
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText(tPt("autoActivities.title")).assertDoesNotExist()
        composeRule.onNodeWithText(tPt("scheduledPause.buttonLabel")).assertDoesNotExist()
    }

    @Test
    fun settingsDialog_dismissCallbackFires() {
        var dismissed = false
        composeRule.setContent {
            CheckingTheme {
                SettingsDialog(
                    currentLanguage = "pt",
                    onLanguageSelected = {},
                    isAuthenticated = false,
                    hasPassword = false,
                    onResetPasswordClick = {},
                    onAutoActivitiesClick = {},
                    onScheduledPauseClick = {},
                    onPermissionsClick = {},
                    onNotificationsClick = {},
                    onSupportClick = {},
                    onAboutClick = {},
                    onDismiss = { dismissed = true },
                    t = { key, values -> t(key, values, "pt") },
                )
            }
        }

        composeRule.onNodeWithText(tPt("settings.backButton")).performClick()
        assertTrue(dismissed)
    }

    @Test
    fun settingsDialog_englishLanguage_rendersEnglishStrings() {
        composeRule.setContent {
            CheckingTheme {
                SettingsDialog(
                    currentLanguage = "en",
                    onLanguageSelected = {},
                    isAuthenticated = false,
                    hasPassword = false,
                    onResetPasswordClick = {},
                    onAutoActivitiesClick = {},
                    onScheduledPauseClick = {},
                    onPermissionsClick = {},
                    onNotificationsClick = {},
                    onSupportClick = {},
                    onAboutClick = {},
                    onDismiss = {},
                    t = { key, values -> t(key, values, "en") },
                )
            }
        }

        composeRule.onNodeWithText(t("settings.title", null, "en")).assertIsDisplayed()
    }
}
