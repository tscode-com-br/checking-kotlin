package br.com.tscode.checking.presentation.about

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingDivider
import br.com.tscode.checking.presentation.theme.CheckingHeaderBg
import br.com.tscode.checking.presentation.theme.CheckingOnPrimary
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingSurfaceEnd
import br.com.tscode.checking.presentation.theme.CheckingSurfaceStart
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong

/**
 * "Sobre" screen: renders the whole system overview (history, the parts of the system, the check-in/out
 * situation rules, and the general notes) from the `about.*` i18n block. Content is plain text using a
 * tiny markup understood by [RichBody]:
 *   - line starting with "## " → sub-heading
 *   - line starting with "• "  → bullet
 *   - line starting with "! "  → highlighted callout (important note)
 *   - blank line               → paragraph break
 *   - anything else            → paragraph
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    t: TranslateFunction,
) {
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
                text = t("about.heading", null),
                style = MaterialTheme.typography.titleMedium,
                color = CheckingOnPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Scrollable body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // §0 — history
            Section(title = t("about.introTitle", null), body = t("about.introBody", null))

            // §1 — the parts of the system
            Section(title = t("about.partsTitle", null), body = t("about.partsIntro", null))
            SubPart(title = t("about.partApiTitle", null), body = t("about.partApiBody", null))
            SubPart(title = t("about.partWebsiteTitle", null), body = t("about.partWebsiteBody", null))
            SubPart(title = t("about.partWebappTitle", null), body = t("about.partWebappBody", null))
            SubPart(title = t("about.partTransportTitle", null), body = t("about.partTransportBody", null))
            SubPart(title = t("about.partAndroidTitle", null), body = t("about.partAndroidBody", null))

            // §2 — situation rules
            Section(title = t("about.rulesTitle", null), body = t("about.rulesIntro", null))
            SubPart(title = t("about.rulesWebTitle", null), body = t("about.rulesWebBody", null))
            SubPart(title = t("about.rulesNativeTitle", null), body = t("about.rulesNativeBody", null))

            // general notes
            Section(title = t("about.notesTitle", null), body = t("about.notesBody", null))

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** A top-level section: a primary title followed by its rich body and a divider. */
@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CheckingPrimary,
            fontWeight = FontWeight.Bold,
        )
        RichBody(body)
        HorizontalDivider(color = CheckingDivider, modifier = Modifier.padding(top = 4.dp))
    }
}

/** A nested part (e.g., one component of the system, or one rules block) with a smaller title. */
@Composable
private fun SubPart(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = CheckingTextStrong,
            fontWeight = FontWeight.SemiBold,
        )
        RichBody(body)
        HorizontalDivider(color = CheckingDivider, modifier = Modifier.padding(top = 4.dp))
    }
}

/** Renders the tiny markup described in [AboutScreen]'s KDoc. */
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
