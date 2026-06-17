package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.check.PasswordChangeFields
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun PasswordChangeDialog(
    fields: PasswordChangeFields,
    hasPassword: Boolean,
    onOldPwChanged: (String) -> Unit,
    onNewPwChanged: (String) -> Unit,
    onConfirmPwChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = if (hasPassword) t("passwordDialog.titleChange", null)
                   else t("passwordDialog.titleRegister", null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )

        HorizontalDivider(color = CheckingDivider)

        if (hasPassword) {
            PasswordField(
                value = fields.oldPw,
                onValueChange = onOldPwChanged,
                label = t("passwordDialog.oldPasswordLabel", null),
                enabled = !fields.isBusy,
                showPasswordLabel = t("auth.showPasswordAria", null),
                hidePasswordLabel = t("auth.hidePasswordAria", null),
            )
        }

        PasswordField(
            value = fields.newPw,
            onValueChange = onNewPwChanged,
            label = t("passwordDialog.newPasswordLabel", null),
            enabled = !fields.isBusy,
            showPasswordLabel = t("auth.showPasswordAria", null),
            hidePasswordLabel = t("auth.hidePasswordAria", null),
        )

        PasswordField(
            value = fields.confirmPw,
            onValueChange = onConfirmPwChanged,
            label = t("passwordDialog.confirmPasswordLabel", null),
            imeAction = ImeAction.Done,
            enabled = !fields.isBusy,
            showPasswordLabel = t("auth.showPasswordAria", null),
            hidePasswordLabel = t("auth.hidePasswordAria", null),
        )

        if (fields.errorMessage.isNotEmpty()) {
            Text(
                text = fields.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = CheckingError,
            )
        }

        if (fields.isBusy) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Tokens.iconSmall),
                    strokeWidth = 2.dp,
                    color = CheckingPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasPassword) t("passwordDialog.changingStatus", null)
                           else t("passwordDialog.savingStatus", null),
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
            }
        }

        PrimaryButton(
            text = if (hasPassword) t("passwordDialog.submitChangeButton", null)
                   else t("passwordDialog.submitRegisterButton", null),
            onClick = onSubmit,
            enabled = !fields.isBusy,
        )

        TextButton(
            onClick = onDismiss,
            enabled = !fields.isBusy,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = t("passwordDialog.backButton", null),
                color = CheckingTextMuted,
            )
        }
    }
}
