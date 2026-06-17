package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.i18n.SUPPORTED_LANGUAGES
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingChoiceSelectedBg
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun SettingsDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    isAuthenticated: Boolean,
    hasPassword: Boolean,
    onResetPasswordClick: () -> Unit,
    onAutoActivitiesClick: () -> Unit,
    onPermissionsClick: () -> Unit,
    onScheduledPauseClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSupportClick: () -> Unit,
    onAboutClick: () -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
) {
    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = t("settings.title", null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = Tokens.sectionGap),
            color = CheckingDivider,
        )

        // a) Language dropdown
        Text(
            text = t("settings.languageLabel", null),
            style = MaterialTheme.typography.labelMedium,
            color = CheckingTextMuted,
        )
        LanguageDropdown(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
        )

        // b) Separator below languages
        HorizontalDivider(
            modifier = Modifier.padding(vertical = Tokens.sectionGap),
            color = CheckingDivider,
        )

        // Account action (kept from before; auth-gated). Placed first in the action group.
        if (isAuthenticated && hasPassword) {
            PrimaryButton(
                text = t("settings.resetPasswordLabel", null),
                onClick = onResetPasswordClick,
            )
        }

        // c) Atividades Automáticas (auth-gated)
        if (isAuthenticated) {
            PrimaryButton(
                text = t("autoActivities.title", null),
                onClick = onAutoActivitiesClick,
            )
        }

        // d) Permissões — relevant regardless of auth (device-level grants)
        PrimaryButton(
            text = t("settings.permissionsLabel", null),
            onClick = onPermissionsClick,
        )

        // d) Pausa Programada + Notificações (auth-gated)
        if (isAuthenticated) {
            PrimaryButton(
                text = t("scheduledPause.buttonLabel", null),
                onClick = onScheduledPauseClick,
            )
            PrimaryButton(
                text = t("settings.notificationsLabel", null),
                onClick = onNotificationsClick,
            )
        }

        // e) Separator before Suporte
        HorizontalDivider(
            modifier = Modifier.padding(vertical = Tokens.sectionGap),
            color = CheckingDivider,
        )

        // f) Suporte
        PrimaryButton(
            text = t("settings.supportLabel", null),
            onClick = onSupportClick,
        )

        // g) Sobre
        PrimaryButton(
            text = t("settings.aboutLabel", null),
            onClick = onAboutClick,
        )

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = t("settings.backButton", null),
                color = CheckingPrimary,
            )
        }
    }
}

/**
 * Single-select language dropdown, styled to match the Projects/Local dropdowns:
 * a field-like trigger showing the current language's native label, expanding to the
 * full list of supported languages.
 */
@Composable
private fun LanguageDropdown(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(Tokens.controlRadius)
    val currentLabel = SUPPORTED_LANGUAGES.find { it.code == currentLanguage }?.nativeLabel
        ?: SUPPORTED_LANGUAGES.firstOrNull()?.nativeLabel
        ?: currentLanguage

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Tokens.controlHeight)
            .clip(shape)
            .background(CheckingCardBg)
            .border(BorderStroke(1.dp, CheckingInputBorder), shape)
            .clickable { expanded = !expanded }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = currentLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = CheckingTextStrong,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = CheckingTextMuted,
        )
    }

    if (expanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CheckingCardBg)
                .border(BorderStroke(1.dp, CheckingInputBorder), shape)
                .padding(vertical = 4.dp),
        ) {
            SUPPORTED_LANGUAGES.forEach { lang ->
                val isSelected = lang.code == currentLanguage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onLanguageSelected(lang.code)
                            expanded = false
                        }
                        .background(if (isSelected) CheckingChoiceSelectedBg else CheckingCardBg)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = lang.nativeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) CheckingPrimary else CheckingTextStrong,
                    )
                }
            }
        }
    }
}
