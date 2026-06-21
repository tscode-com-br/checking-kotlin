package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import br.com.tscode.checking.presentation.check.SelfRegistrationFields
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun SelfRegistrationDialog(
    fields: SelfRegistrationFields,
    onNomeChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPwChanged: (String) -> Unit,
    onProjectToggled: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CheckingPrimary,
        focusedLabelColor = CheckingPrimary,
        cursorColor = CheckingPrimary,
        unfocusedBorderColor = CheckingInputBorder,
    )

    DialogScaffold(onDismiss = onDismiss) {
        // plan003 (decision 3) — top-left Back arrow returns to the main screen (e.g. if the user typed an
        // already-registered key by mistake). Reuses onDismiss (which also blocks the dialog auto-reopen).
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss, enabled = !fields.isBusy) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = t("settings.backButton", null),
                    tint = CheckingPrimary,
                )
            }
            Text(
                text = t("registrationDialog.title", null),
                style = MaterialTheme.typography.titleLarge,
                color = CheckingTextStrong,
            )
        }

        HorizontalDivider(color = CheckingDivider)

        OutlinedTextField(
            value = fields.chave,
            onValueChange = {},
            label = { Text(t("registrationDialog.keyLabel", null)) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = CheckingInputBorder,
                focusedBorderColor = CheckingInputBorder,
                disabledBorderColor = CheckingInputBorder,
            ),
        )

        OutlinedTextField(
            value = fields.nome,
            onValueChange = onNomeChanged,
            label = { Text(t("registrationDialog.fullNameLabel", null)) },
            singleLine = true,
            enabled = !fields.isBusy,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = inputColors,
        )

        OutlinedTextField(
            value = fields.email,
            onValueChange = onEmailChanged,
            label = { Text(t("registrationDialog.emailLabel", null)) },
            placeholder = { Text(t("registrationDialog.emailPlaceholder", null)) },
            singleLine = true,
            enabled = !fields.isBusy,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = inputColors,
        )

        PasswordField(
            value = fields.password,
            onValueChange = onPasswordChanged,
            label = t("registrationDialog.passwordLabel", null),
            enabled = !fields.isBusy,
            showPasswordLabel = t("auth.showPasswordAria", null),
            hidePasswordLabel = t("auth.hidePasswordAria", null),
        )

        PasswordField(
            value = fields.confirmPw,
            onValueChange = onConfirmPwChanged,
            label = t("registrationDialog.confirmPasswordLabel", null),
            imeAction = ImeAction.Done,
            enabled = !fields.isBusy,
            showPasswordLabel = t("auth.showPasswordAria", null),
            hidePasswordLabel = t("auth.hidePasswordAria", null),
        )

        HorizontalDivider(color = CheckingDivider)

        Text(
            text = t("registrationDialog.projectsLabel", null),
            style = MaterialTheme.typography.labelMedium,
            color = CheckingTextMuted,
        )

        when {
            fields.isLoadingProjects -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Tokens.iconSmall),
                        strokeWidth = 2.dp,
                        color = CheckingPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = t("registrationDialog.loadingProjects", null),
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingTextMuted,
                    )
                }
            }

            fields.projectCatalog.isEmpty() -> {
                Text(
                    text = t("registrationDialog.noProjectsAvailable", null),
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    fields.projectCatalog.forEach { project ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !fields.isBusy) { onProjectToggled(project.id) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = fields.selectedProjectIds.contains(project.id),
                                onCheckedChange = { onProjectToggled(project.id) },
                                enabled = !fields.isBusy,
                                colors = CheckboxDefaults.colors(checkedColor = CheckingPrimary),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CheckingTextStrong,
                            )
                        }
                    }
                }
            }
        }

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
                    text = t("registrationDialog.submittingStatus", null),
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
            }
        }

        PrimaryButton(
            text = t("registrationDialog.submitButton", null),
            onClick = onSubmit,
            enabled = !fields.isBusy,
        )

        TextButton(
            onClick = onDismiss,
            enabled = !fields.isBusy,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = t("registrationDialog.backButton", null),
                color = CheckingTextMuted,
            )
        }
    }
}
