package br.com.tscode.checking.presentation.privacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.presentation.components.PrimaryButton
import br.com.tscode.checking.presentation.components.SecondaryButton
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingHeaderBg
import br.com.tscode.checking.presentation.theme.CheckingOnPrimary
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingSurfaceEnd
import br.com.tscode.checking.presentation.theme.CheckingSurfaceStart
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.privacy.PrivacyConfig

/**
 * "Privacidade e Proteção de Dados" — the LGPD (Lei 13.709/2018) transparency + data-subject-rights
 * surface (arts. 9, 18, 20, 33, 41). All legal facts come from [PrivacyConfig]; all wording from the
 * `privacy.*` i18n block. Body markup matches "Sobre": "## ", "• ", "! ", blank line = paragraph.
 */
@Composable
fun PrivacyScreen(
    chave: String,
    onBack: () -> Unit,
    onDeleteLocalData: (onDone: () -> Unit) -> Unit,
    t: TranslateFunction,
) {
    val context = LocalContext.current

    // Interpolation values shared by every section (art. 9 requires controller + contact + purpose etc.).
    val vals = mapOf(
        "controller" to PrivacyConfig.controllerLegalName,
        "privacyEmail" to PrivacyConfig.privacyRequestsEmail,
        "hostingCountry" to PrivacyConfig.hostingCountry,
        "retentionHistory" to PrivacyConfig.retentionCheckHistory,
        "retentionVideo" to PrivacyConfig.retentionAccidentVideo,
        "retentionLocalDays" to PrivacyConfig.retentionLocalLogDays.toString(),
        "minAge" to PrivacyConfig.minimumAge.toString(),
        "chave" to chave,
    )

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletedNotice by remember { mutableStateOf(false) }

    fun sendPrivacyEmail() {
        val subject = t("privacy.contactSubject", vals)
        val body = t("privacy.contactBody", vals)
        val uri = Uri.parse("mailto:${PrivacyConfig.privacyRequestsEmail}")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openFullPolicy() {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PrivacyConfig.privacyPolicyUrl)))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CheckingSurfaceStart, CheckingSurfaceEnd)))
            .systemBarsPadding(),
    ) {
        // Header bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(CheckingHeaderBg),
            contentAlignment = Alignment.CenterStart,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = t("settings.backButton", null),
                    tint = CheckingOnPrimary,
                )
            }
            Text(
                text = t("privacy.heading", null),
                style = MaterialTheme.typography.titleMedium,
                color = CheckingOnPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RichBody(t("privacy.intro", vals))

            if (!PrivacyConfig.isConfigured) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CheckingCardBg, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = t("privacy.configPendingNotice", null),
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingError,
                    )
                }
            }

            Section(t("privacy.purposeTitle", null), t("privacy.purposeBody", vals))
            Section(t("privacy.dataTitle", null), t("privacy.dataBody", vals))
            Section(t("privacy.legalBasisTitle", null), t("privacy.legalBasisBody", vals))
            Section(t("privacy.retentionTitle", null), t("privacy.retentionBody", vals))
            Section(t("privacy.sharingTitle", null), t("privacy.sharingBody", vals))
            Section(t("privacy.controllerTitle", null), t("privacy.controllerBody", vals))
            Section(t("privacy.rightsTitle", null), t("privacy.rightsBody", vals))
            Section(t("privacy.automatedTitle", null), t("privacy.automatedBody", vals))
            Section(t("privacy.securityTitle", null), t("privacy.securityBody", vals))
            Section(t("privacy.ageTitle", null), t("privacy.ageBody", vals))

            // ── Rights actions (art. 18 — facilitated + free) ──────────────────────
            PrimaryButton(text = t("privacy.contactDpo", null), onClick = { sendPrivacyEmail() })
            SecondaryButton(text = t("privacy.requestAccountDeletion", null), onClick = { sendPrivacyEmail() })
            SecondaryButton(text = t("privacy.deleteLocalData", null), onClick = { showDeleteConfirm = true })
            SecondaryButton(text = t("privacy.openFullPolicy", null), onClick = { openFullPolicy() })

            if (deletedNotice) {
                Text(
                    text = t("privacy.deleteLocalDataDone", null),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CheckingPrimary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(t("privacy.deleteLocalData", null)) },
            text = { Text(t("privacy.deleteLocalDataConfirm", null)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteLocalData { deletedNotice = true }
                }) { Text(t("privacy.confirm", null), color = CheckingError) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(t("privacy.cancel", null), color = CheckingPrimary)
                }
            },
        )
    }
}

/** A section: primary title + rich body + divider (mirrors the About screen). */
@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = CheckingPrimary,
            fontWeight = FontWeight.Bold,
        )
        RichBody(body)
        HorizontalDivider(color = CheckingDivider, modifier = Modifier.padding(top = 4.dp))
    }
}

/** Tiny markup renderer: "## " sub-heading, "• " bullet, "! " callout, blank line = paragraph break. */
@Composable
private fun RichBody(body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        body.split("\n").forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.isEmpty() -> Spacer(modifier = Modifier.height(4.dp))
                line.startsWith("## ") -> Text(
                    text = line.removePrefix("## "),
                    style = MaterialTheme.typography.titleSmall,
                    color = CheckingTextStrong,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                line.startsWith("• ") -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "•", style = MaterialTheme.typography.bodySmall, color = CheckingPrimary)
                    Text(
                        text = line.removePrefix("• "),
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingTextStrong,
                    )
                }
                line.startsWith("! ") -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CheckingCardBg, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = line.removePrefix("! "),
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingTextMuted,
                    )
                }
                else -> Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CheckingTextStrong,
                )
            }
        }
    }
}
