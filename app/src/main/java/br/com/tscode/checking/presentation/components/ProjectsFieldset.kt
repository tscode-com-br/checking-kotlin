package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.domain.model.Project
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMuted
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

/**
 * Project membership — a dropdown box with multi-select checkboxes (web
 * `projectMembershipOptions`). A user may belong to several projects at once, so this is
 * a checkbox list (not radios). At least one project must stay selected (enforced upstream).
 */
@Composable
fun ProjectsFieldset(
    catalog: List<Project>,
    memberships: List<String>,
    isLoading: Boolean,
    onMembershipToggled: (String) -> Unit,
    t: TranslateFunction,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(Tokens.controlRadius)

    // Options come from the full catalogue; if it hasn't loaded yet, fall back to the
    // current memberships so the user still sees their projects.
    val options = if (catalog.isNotEmpty()) catalog.map { it.name } else memberships

    val summary = when {
        memberships.isNotEmpty() -> memberships.joinToString(", ")
        isLoading -> t("projects.loadingProjects", null)
        else -> t("projects.noneAvailableShort", null)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
    ) {
        Text(
            text = t("projects.label", null),
            style = MaterialTheme.typography.labelLarge,
            color = CheckingTextStrong,
        )

        // Dropdown trigger styled like a field.
        // No .clip() here: the rounded look comes from background(shape) + border(shape).
        // Clipping the Row truncated the trigger text's descenders on the first (min-height)
        // layout pass; dropping it lets the text render in full.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Tokens.controlHeight)
                .background(CheckingCardBg, shape)
                .border(BorderStroke(1.dp, CheckingInputBorder), shape)
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = summary,
                // includeFontPadding=true restores the glyph's asc/descent padding (Compose
                // defaults it to false), giving the single line enough vertical room so its
                // bottom isn't visually clipped inside the field.
                style = MaterialTheme.typography.bodyLarge.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = true),
                ),
                color = CheckingTextStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = CheckingTextMuted,
            )
        }

        // Expandable checkbox panel.
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(CheckingCardBg)
                    .border(BorderStroke(1.dp, CheckingInputBorder), shape)
                    .padding(vertical = 4.dp),
            ) {
                if (options.isEmpty()) {
                    Text(
                        text = if (isLoading) {
                            t("projects.loadingProjects", null)
                        } else {
                            t("projects.noneAvailableSentence", null)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = CheckingTextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                } else {
                    options.forEach { name ->
                        val checked = memberships.contains(name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isLoading) { onMembershipToggled(name) }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { if (!isLoading) onMembershipToggled(name) },
                                colors = CheckboxDefaults.colors(checkedColor = CheckingPrimary),
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (checked) CheckingPrimary else CheckingTextStrong,
                            )
                        }
                    }
                }
            }
        }
    }
}
