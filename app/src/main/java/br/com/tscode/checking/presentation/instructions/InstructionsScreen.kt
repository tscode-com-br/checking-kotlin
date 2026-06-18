package br.com.tscode.checking.presentation.instructions

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
import br.com.tscode.checking.presentation.theme.Tokens

/**
 * Step-by-step tutorial ("Instruções"): how to use the app, enable Automatic Mode, and set up the
 * Scheduled Pause. Content comes from the `instructions.*` i18n block; mirrors ManualScreen's look.
 */
@Composable
fun InstructionsScreen(
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
                .height(Tokens.headerHeight)
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
                text = t("instructions.heading", null),
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
                .padding(Tokens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
        ) {
            Text(
                text = t("instructions.intro", null),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingTextStrong,
            )

            HorizontalDivider(color = CheckingDivider)

            StepSection(
                index = "1", titleKey = "instructions.step1.title",
                itemKeys = listOf("instructions.step1.item1", "instructions.step1.item2", "instructions.step1.item3"),
                t = t,
            )
            StepSection(
                index = "2", titleKey = "instructions.step2.title",
                itemKeys = listOf("instructions.step2.item1", "instructions.step2.item2", "instructions.step2.item3"),
                t = t,
            )
            StepSection(
                index = "3", titleKey = "instructions.step3.title", leadKey = "instructions.step3.lead",
                itemKeys = listOf(
                    "instructions.step3.item1", "instructions.step3.item2",
                    "instructions.step3.item3", "instructions.step3.item4",
                ),
                calloutKey = "instructions.step3.callout",
                t = t,
            )
            StepSection(
                index = "4", titleKey = "instructions.step4.title", leadKey = "instructions.step4.lead",
                itemKeys = listOf(
                    "instructions.step4.item1", "instructions.step4.item2",
                    "instructions.step4.item3", "instructions.step4.item4",
                ),
                t = t,
            )
            StepSection(
                index = "5", titleKey = "instructions.step5.title",
                itemKeys = listOf(
                    "instructions.step5.item1", "instructions.step5.item2", "instructions.step5.item3",
                ),
                t = t,
            )
            StepSection(
                index = "6", titleKey = "instructions.step6.title",
                itemKeys = listOf(
                    "instructions.step6.item1", "instructions.step6.item2", "instructions.step6.item3",
                ),
                t = t,
            )
            StepSection(
                index = "7", titleKey = "instructions.step7.title", leadKey = "instructions.step7.lead",
                itemKeys = listOf(
                    "instructions.step7.item1", "instructions.step7.item2",
                    "instructions.step7.item3", "instructions.step7.item4",
                ),
                t = t,
            )
            StepSection(
                index = "8", titleKey = "instructions.step8.title",
                itemKeys = listOf(
                    "instructions.step8.item1", "instructions.step8.item2", "instructions.step8.item3",
                    "instructions.step8.item4", "instructions.step8.item5",
                ),
                t = t,
            )

            Text(
                text = t("instructions.closing", null),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(Tokens.sectionGap))
        }
    }
}

@Composable
private fun StepSection(
    index: String,
    titleKey: String,
    itemKeys: List<String>,
    t: TranslateFunction,
    leadKey: String? = null,
    calloutKey: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = index,
                style = MaterialTheme.typography.labelLarge,
                color = CheckingPrimary,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = t(titleKey, null), style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
                if (leadKey != null) {
                    Text(text = t(leadKey, null), style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
                }
            }
        }
        itemKeys.forEach { key ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "•", style = MaterialTheme.typography.bodySmall, color = CheckingPrimary)
                Text(text = t(key, null), style = MaterialTheme.typography.bodySmall, color = CheckingTextStrong)
            }
        }
        if (calloutKey != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CheckingCardBg, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                Text(text = t(calloutKey, null), style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
            }
        }
        HorizontalDivider(color = CheckingDivider, modifier = Modifier.padding(top = 4.dp))
    }
}
