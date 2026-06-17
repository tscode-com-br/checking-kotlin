package br.com.tscode.checking.presentation.manual

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.R
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

@Composable
fun ManualScreen(
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
                text = t("manual.heading", null),
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
            // Hero intro
            Text(
                text = t("manual.introPrimary", null),
                style = MaterialTheme.typography.bodyMedium,
                color = CheckingTextStrong,
            )
            Text(
                text = t("manual.introSecondary", null),
                style = MaterialTheme.typography.bodySmall,
                color = CheckingTextMuted,
            )

            // Highlight cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HighlightCard(
                    title = t("manual.highlights.accessTitle", null),
                    body = t("manual.highlights.accessBody", null),
                    modifier = Modifier.weight(1f),
                )
                HighlightCard(
                    title = t("manual.highlights.locationTitle", null),
                    body = t("manual.highlights.locationBody", null),
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider(color = CheckingDivider)

            // Section 01 — Overview
            ManualSection(
                index = "01",
                title = t("manual.sections.overview.title", null),
                lead = t("manual.sections.overview.lead", null),
                items = listOf(
                    t("manual.sections.overview.item1", null),
                    t("manual.sections.overview.item2", null),
                    t("manual.sections.overview.item3", null),
                ),
                imageRes = R.drawable.manual_auth_shell,
                imageCaption = t("manual.sections.overview.figureCaption", null),
                t = t,
            )

            // Section 02 — Auth flow
            ManualSection(
                index = "02",
                title = t("manual.sections.authFlow.title", null),
                lead = t("manual.sections.authFlow.lead", null),
                items = listOf(
                    t("manual.sections.authFlow.item1", null),
                    t("manual.sections.authFlow.item2", null),
                    t("manual.sections.authFlow.item3", null),
                ),
                callout = t("manual.sections.authFlow.note", null),
                t = t,
            )

            // Section 03 — User registration
            ManualSection(
                index = "03",
                title = t("manual.sections.userRegistration.title", null),
                lead = t("manual.sections.userRegistration.lead", null),
                items = listOf(
                    t("manual.sections.userRegistration.item1", null),
                    t("manual.sections.userRegistration.item2", null),
                    t("manual.sections.userRegistration.item3", null),
                ),
                imageRes = R.drawable.manual_user_registration,
                imageCaption = t("manual.sections.userRegistration.figureCaption", null),
                t = t,
            )

            // Section 04 — Password registration
            ManualSection(
                index = "04",
                title = t("manual.sections.passwordRegistration.title", null),
                lead = t("manual.sections.passwordRegistration.lead", null),
                items = listOf(
                    t("manual.sections.passwordRegistration.item1", null),
                    t("manual.sections.passwordRegistration.item2", null),
                    t("manual.sections.passwordRegistration.item3", null),
                ),
                imageRes = R.drawable.manual_password_registration,
                imageCaption = t("manual.sections.passwordRegistration.figureCaption", null),
                t = t,
            )

            // Section 05 — Login
            ManualSection(
                index = "05",
                title = t("manual.sections.login.title", null),
                lead = t("manual.sections.login.lead", null),
                items = listOf(
                    t("manual.sections.login.item1", null),
                    t("manual.sections.login.item2", null),
                    t("manual.sections.login.item3", null),
                ),
                t = t,
            )

            // Section 06 — Attendance
            ManualSection(
                index = "06",
                title = t("manual.sections.attendance.title", null),
                lead = t("manual.sections.attendance.lead", null),
                items = listOf(
                    t("manual.sections.attendance.item1", null),
                    t("manual.sections.attendance.item2", null),
                    t("manual.sections.attendance.item3", null),
                ),
                imageRes = R.drawable.manual_check_success,
                imageCaption = t("manual.sections.attendance.figureCaption", null),
                t = t,
            )

            // Section 07 — Project selection
            ManualSection(
                index = "07",
                title = t("manual.sections.projectSelection.title", null),
                lead = t("manual.sections.projectSelection.lead", null),
                items = listOf(
                    t("manual.sections.projectSelection.item1", null),
                    t("manual.sections.projectSelection.item2", null),
                    t("manual.sections.projectSelection.item3", null),
                ),
                imageRes = R.drawable.manual_project_selection,
                imageCaption = t("manual.sections.projectSelection.figureCaption", null),
                t = t,
            )

            // Section 08 — Location
            ManualSection(
                index = "08",
                title = t("manual.sections.location.title", null),
                lead = t("manual.sections.location.lead", null),
                items = listOf(
                    t("manual.sections.location.item1", null),
                    t("manual.sections.location.item2", null),
                    t("manual.sections.location.item3", null),
                ),
                imageRes = R.drawable.manual_location_granted,
                imageCaption = t("manual.sections.location.figureCaptionGranted", null),
                t = t,
            )

            // Section 09 — Automatic activities
            ManualSection(
                index = "09",
                title = t("manual.sections.automaticActivities.title", null),
                lead = t("manual.sections.automaticActivities.lead", null),
                items = listOf(
                    t("manual.sections.automaticActivities.item1", null),
                    t("manual.sections.automaticActivities.item2", null),
                    t("manual.sections.automaticActivities.item3", null),
                ),
                t = t,
            )

            // Section 10 — Transport
            ManualSection(
                index = "10",
                title = t("manual.sections.transport.title", null),
                lead = t("manual.sections.transport.lead", null),
                items = listOf(
                    t("manual.sections.transport.item1", null),
                    t("manual.sections.transport.item2", null),
                    t("manual.sections.transport.item3", null),
                ),
                imageRes = R.drawable.manual_transport_screen,
                imageCaption = t("manual.sections.transport.figureCaption", null),
                t = t,
            )

            // Section 11 — Password change
            ManualSection(
                index = "11",
                title = t("manual.sections.passwordChange.title", null),
                lead = t("manual.sections.passwordChange.lead", null),
                items = listOf(
                    t("manual.sections.passwordChange.item1", null),
                    t("manual.sections.passwordChange.item2", null),
                    t("manual.sections.passwordChange.item3", null),
                ),
                imageRes = R.drawable.manual_password_change,
                imageCaption = t("manual.sections.passwordChange.figureCaption", null),
                t = t,
            )

            // Section 12 — Settings
            ManualSection(
                index = "12",
                title = t("manual.sections.settings.title", null),
                lead = t("manual.sections.settings.lead", null),
                items = listOf(
                    t("manual.sections.settings.item1", null),
                    t("manual.sections.settings.item2", null),
                    t("manual.sections.settings.item3", null),
                ),
                imageRes = R.drawable.manual_settings_modal,
                imageCaption = t("manual.sections.settings.figureCaption", null),
                t = t,
            )

            // Section 13 — Support
            ManualSection(
                index = "13",
                title = t("manual.sections.support.title", null),
                lead = t("manual.sections.support.lead", null),
                items = listOf(
                    t("manual.sections.support.item1", null),
                    t("manual.sections.support.item2", null),
                    t("manual.sections.support.item3", null),
                ),
                t = t,
            )

            // Section 14 — FAQ
            FaqSection(
                index = "14",
                title = t("manual.sections.faq.title", null),
                lead = t("manual.sections.faq.lead", null),
                items = listOf(
                    t("manual.sections.faq.q1", null) to t("manual.sections.faq.a1", null),
                    t("manual.sections.faq.q2", null) to t("manual.sections.faq.a2", null),
                    t("manual.sections.faq.q3", null) to t("manual.sections.faq.a3", null),
                ),
            )

            Spacer(modifier = Modifier.height(Tokens.sectionGap))
        }
    }
}

// ---- Private composables ----

@Composable
private fun HighlightCard(title: String, body: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Tokens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = CheckingPrimary)
            Text(text = body, style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
        }
    }
}

@Composable
private fun ManualSection(
    index: String,
    title: String,
    lead: String,
    items: List<String>,
    @DrawableRes imageRes: Int? = null,
    imageCaption: String? = null,
    callout: String? = null,
    t: TranslateFunction,
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
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
                Text(text = lead, style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
            }
        }
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "•", style = MaterialTheme.typography.bodySmall, color = CheckingPrimary)
                Text(text = item, style = MaterialTheme.typography.bodySmall, color = CheckingTextStrong)
            }
        }
        if (callout != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CheckingCardBg, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                Text(text = callout, style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
            }
        }
        if (imageRes != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = imageCaption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(Tokens.cardRadius)),
                    contentScale = ContentScale.Crop,
                )
                if (imageCaption != null) {
                    Text(
                        text = imageCaption,
                        style = MaterialTheme.typography.labelSmall,
                        color = CheckingTextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
        HorizontalDivider(color = CheckingDivider, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun FaqSection(
    index: String,
    title: String,
    lead: String,
    items: List<Pair<String, String>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = index, style = MaterialTheme.typography.labelLarge, color = CheckingPrimary, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = CheckingTextStrong)
                Text(text = lead, style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
            }
        }
        items.forEach { (q, a) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = q, style = MaterialTheme.typography.labelMedium, color = CheckingTextStrong, fontWeight = FontWeight.SemiBold)
                    Text(text = a, style = MaterialTheme.typography.bodySmall, color = CheckingTextMuted)
                }
            }
        }
    }
}
