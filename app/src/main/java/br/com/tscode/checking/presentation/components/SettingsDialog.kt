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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.i18n.SUPPORTED_LANGUAGES
import br.com.tscode.checking.presentation.check.AutoActivitiesHealth
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingChoiceSelectedBg
import br.com.tscode.checking.presentation.theme.CheckingFieldAuthedBorder
import br.com.tscode.checking.presentation.theme.CheckingFieldPendingBorder
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
    onScheduledPauseClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSupportClick: () -> Unit,
    onAboutClick: () -> Unit,
    onDismiss: () -> Unit,
    t: (String, Map<String, String>?) -> String,
    autoActivitiesHealth: AutoActivitiesHealth = AutoActivitiesHealth.Off,
    onInstructionsClick: () -> Unit = {},
    onManualClick: () -> Unit = {},
) {
    // P3.1 — grouped rows under section headers (replaces the old flat wall of PrimaryButtons).
    // Signature, callbacks, auth-gating and the visible t() keys are unchanged; only the layout differs.
    DialogScaffold(onDismiss = onDismiss) {
        Text(
            text = t("settings.title", null),
            style = MaterialTheme.typography.titleLarge,
            color = CheckingTextStrong,
        )

        // ── Group 1: Atividades Automáticas ──────────────────────────────────────
        SettingsGroupHeader(t("settings.groupAutoActivities", null))
        if (isAuthenticated) {
            // P3.2 — trailing status chip driven by AutoActivitiesHealth (same palette as the gear glow).
            val (statusKey, statusTone) = when (autoActivitiesHealth) {
                AutoActivitiesHealth.Healthy -> "settings.statusOn" to CheckingFieldAuthedBorder
                AutoActivitiesHealth.Degraded -> "settings.statusAttention" to CheckingFieldPendingBorder
                AutoActivitiesHealth.Off -> "settings.statusOff" to CheckingTextMuted
            }
            SettingsRow(
                icon = Icons.Outlined.MyLocation,
                label = t("autoActivities.title", null),
                onClick = onAutoActivitiesClick,
                trailing = { StatusChip(text = t(statusKey, null), tone = statusTone) },
            )
        }
        // P4.2/P4.3 — the standalone "Permissões" row and its dialog were removed; granting all
        // permissions now lives in the Auto-activities dialog's live checklist (P4.1).

        // ── Group 2: Preferências ────────────────────────────────────────────────
        SettingsGroupHeader(t("settings.groupPreferences", null))
        if (isAuthenticated) {
            SettingsRow(
                icon = Icons.Outlined.Schedule,
                label = t("scheduledPause.buttonLabel", null),
                onClick = onScheduledPauseClick,
            )
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                label = t("settings.notificationsLabel", null),
                onClick = onNotificationsClick,
            )
        }
        // Idioma — the existing language dropdown, kept as-is (label above a field-like trigger).
        Text(
            text = t("settings.languageLabel", null),
            style = MaterialTheme.typography.labelMedium,
            color = CheckingTextMuted,
        )
        LanguageDropdown(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
        )
        if (isAuthenticated && hasPassword) {
            SettingsRow(
                icon = Icons.Outlined.Lock,
                label = t("settings.resetPasswordLabel", null),
                onClick = onResetPasswordClick,
            )
        }

        // ── Group 3: Ajuda ───────────────────────────────────────────────────────
        SettingsGroupHeader(t("settings.groupHelp", null))
        SettingsRow(
            icon = Icons.Outlined.School,
            label = t("settings.instructionsLabel", null),
            onClick = onInstructionsClick,
        )
        SettingsRow(
            icon = Icons.Outlined.MenuBook,
            label = t("settings.manualLabel", null),
            onClick = onManualClick,
        )
        SettingsRow(
            icon = Icons.Outlined.Chat,
            label = t("settings.supportLabel", null),
            onClick = onSupportClick,
        )
        SettingsRow(
            icon = Icons.Outlined.Info,
            label = t("settings.aboutLabel", null),
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

/** Small uppercase muted header that introduces a group of [SettingsRow]s. */
@Composable
private fun SettingsGroupHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = CheckingTextMuted,
        modifier = Modifier.padding(top = Tokens.itemGap),
    )
}

/** A navigation row: leading icon + label + optional trailing content + chevron. */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.controlRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CheckingPrimary,
            modifier = Modifier.size(Tokens.iconSmall),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = CheckingTextStrong,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            trailing()
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = CheckingTextMuted,
            modifier = Modifier.size(Tokens.iconSmall),
        )
    }
}

/** Small outlined pill showing the auto-activities status, tinted with the same palette as the gear glow. */
@Composable
private fun StatusChip(text: String, tone: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = tone,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .border(BorderStroke(1.dp, tone), RoundedCornerShape(percent = 50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
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
