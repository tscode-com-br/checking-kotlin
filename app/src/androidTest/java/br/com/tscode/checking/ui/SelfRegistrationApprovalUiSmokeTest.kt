package br.com.tscode.checking.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.tscode.checking.i18n.t
import br.com.tscode.checking.presentation.check.NotificationTone
import br.com.tscode.checking.presentation.check.SelfRegistrationFields
import br.com.tscode.checking.presentation.components.AuthRow
import br.com.tscode.checking.presentation.components.NotificationCard
import br.com.tscode.checking.presentation.components.SelfRegistrationDialog
import br.com.tscode.checking.presentation.theme.CheckingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * TP5 (plan003) — instrumented smoke for the approval UI, in the style of SettingsDialogSmokeTest.
 *
 * Compose UI tests assert text / contentDescription / click semantics, not pixel colors. So these tests
 * render each component in the exact awaiting/error/dialog state and assert what is queryable:
 *  - AuthRow renders in the awaiting state (the orange Pending glow is a shadow color → verified visually
 *    on-device; the FieldGlow.Pending decision is unit-covered in SelfRegistrationApprovalTest/AuthRow).
 *  - The notification bar renders the awaiting / queue-full message (tone=Error → vivid red on-device).
 *  - The registration dialog shows a top-left Back arrow that dismisses (decision 3).
 *  - The registration dialog renders its content when open (what the unknown-key auto-open produces;
 *    the auto-open trigger is unit-covered in SelfRegistrationApprovalTest).
 *
 * Run on a connected device twice via `am instrument` (never connectedAndroidTest — BootReceiver crashes
 * it). With no device, this is "device verification pending"; it must still compile
 * (compileDebugAndroidTestKotlin green).
 */
class SelfRegistrationApprovalUiSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun tPt(key: String) = t(key, null, "pt")
    private val tFn: (String, Map<String, String>?) -> String = { key, values -> t(key, values, "pt") }

    // 1 — AuthRow in the awaiting state renders the key + password fields (orange glow visual → on-device).
    @Test
    fun authRow_awaiting_rendersKeyAndPasswordFields() {
        composeRule.setContent {
            CheckingTheme {
                AuthRow(
                    chave = "NEW1",
                    onChaveChanged = {},
                    password = "",
                    onPasswordChanged = {},
                    isFound = false,
                    isAuthenticated = false,
                    isStatusLoading = false,
                    isStatusAvailable = true,
                    prompt = "",
                    onSettingsClick = {},
                    onRequestRegistrationClick = {},
                    t = tFn,
                    awaitingApproval = true,
                )
            }
        }

        composeRule.onNodeWithText(tPt("auth.keyLabel")).assertIsDisplayed()
        composeRule.onNodeWithText(tPt("auth.passwordLabel")).assertIsDisplayed()
    }

    // 2 — Error-tone notification renders the awaiting message (vivid red → on-device).
    @Test
    fun awaitingApprovalNotification_error_rendersMessage() {
        composeRule.setContent {
            CheckingTheme {
                NotificationCard(
                    primary = tPt("auth.awaitingApproval"),
                    secondary = "",
                    tone = NotificationTone.Error,
                )
            }
        }

        composeRule.onNodeWithText(tPt("auth.awaitingApproval")).assertIsDisplayed()
    }

    // 2b — Error-tone notification renders the queue-full message (vivid red → on-device).
    @Test
    fun queueFullNotification_error_rendersMessage() {
        composeRule.setContent {
            CheckingTheme {
                NotificationCard(
                    primary = tPt("auth.registrationQueueFull"),
                    secondary = "",
                    tone = NotificationTone.Error,
                )
            }
        }

        composeRule.onNodeWithText(tPt("auth.registrationQueueFull")).assertIsDisplayed()
    }

    // 3 — The registration dialog shows a top-left Back arrow that dismisses (decision 3).
    @Test
    fun selfRegistrationDialog_backArrow_dismisses() {
        var dismissed = false
        composeRule.setContent {
            CheckingTheme {
                SelfRegistrationDialog(
                    fields = SelfRegistrationFields(chave = "NEW1"),
                    onNomeChanged = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onConfirmPwChanged = {},
                    onProjectToggled = {},
                    onSubmit = {},
                    onDismiss = { dismissed = true },
                    t = tFn,
                )
            }
        }

        composeRule.onNodeWithContentDescription(tPt("settings.backButton")).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(tPt("settings.backButton")).performClick()
        assertTrue("Back arrow must invoke onDismiss", dismissed)
    }

    // 4 — The registration dialog renders its content when open (what the unknown-key auto-open shows).
    @Test
    fun selfRegistrationDialog_open_rendersTitleAndKey() {
        composeRule.setContent {
            CheckingTheme {
                SelfRegistrationDialog(
                    fields = SelfRegistrationFields(chave = "NEW1"),
                    onNomeChanged = {},
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onConfirmPwChanged = {},
                    onProjectToggled = {},
                    onSubmit = {},
                    onDismiss = {},
                    t = tFn,
                )
            }
        }

        composeRule.onNodeWithText(tPt("registrationDialog.title")).assertIsDisplayed()
        composeRule.onNodeWithText("NEW1").assertIsDisplayed() // the read-only key field
    }
}
