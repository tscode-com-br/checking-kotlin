package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun AuthRow(
    chave: String,
    onChaveChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    isFound: Boolean,
    isAuthenticated: Boolean,
    isStatusLoading: Boolean,
    isStatusAvailable: Boolean,
    prompt: String,
    onSettingsClick: () -> Unit,
    onRequestRegistrationClick: () -> Unit,
    t: (String, Map<String, String>?) -> String,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // The colored glow mirrors the web's auth-field state classes:
    //   authenticated → green, key recognized (awaiting login) → orange.
    val glow = when {
        isAuthenticated -> FieldGlow.Authenticated
        isFound -> FieldGlow.Pending
        else -> FieldGlow.None
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            LabeledField(
                    label = t("auth.keyLabel", null),
                    value = chave,
                    onValueChange = { raw ->
                        onChaveChanged(raw.uppercase().filter { it.isLetterOrDigit() }.take(4))
                    },
                    glow = glow,
                    placeholder = t("auth.keyPlaceholder", null),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Text,
                        imeAction = if (isFound) ImeAction.Next else ImeAction.Done,
                    ),
                    trailingIcon = if (isStatusLoading) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Tokens.iconSmall),
                                strokeWidth = 2.dp,
                                color = CheckingPrimary,
                            )
                        }
                    } else null,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f),
                )

                LabeledField(
                    label = t("auth.passwordLabel", null),
                    value = password,
                    onValueChange = onPasswordChanged,
                    glow = glow,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.size(Tokens.iconLarge),
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    t("auth.hidePasswordAria", null)
                                } else {
                                    t("auth.showPasswordAria", null)
                                },
                                tint = CheckingTextMuted,
                                modifier = Modifier.size(Tokens.iconSmall),
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                )

                // Settings gear — bottom-aligned with the inputs (label sits above them).
                Box(
                    modifier = Modifier.height(Tokens.controlHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = t("auth.openSettingsAria", null),
                            tint = CheckingPrimary,
                        )
                    }
                }
            }

            if (prompt.isNotEmpty()) {
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = CheckingTextMuted,
                )
            }

            if (isStatusAvailable && !isFound) {
                TextButton(onClick = onRequestRegistrationClick) {
                    Text(
                        text = t("auth.requestRegistrationButton", null),
                        color = CheckingPrimary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
    }
}
