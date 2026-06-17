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
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingChoiceSelectedBg
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMutedSoft
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

/**
 * "Local" dropdown — single-select list of every registered location across the user's
 * projects. Styled to match the Projects dropdown (label above + field-like trigger +
 * expandable panel). Shown for manual check-in (automatic activities off / Situation 9).
 */
@Composable
fun LocationSelectField(
    locations: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
    t: TranslateFunction,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(Tokens.controlRadius)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
    ) {
        Text(
            text = t("location.title", null),
            style = MaterialTheme.typography.labelLarge,
            color = CheckingTextStrong,
        )

        // No .clip() here: rounded look from background(shape) + border(shape), without
        // truncating the trigger text. (Matches ProjectsFieldset.)
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
                text = selected ?: t("location.manualSelectPlaceholder", null),
                style = MaterialTheme.typography.bodyLarge.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = true),
                ),
                color = if (selected != null) CheckingTextStrong else CheckingTextMutedSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = CheckingTextMutedSoft,
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
                locations.forEach { loc ->
                    val isSelected = loc == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(loc)
                                expanded = false
                            }
                            .background(if (isSelected) CheckingChoiceSelectedBg else CheckingCardBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = loc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) CheckingPrimary else CheckingTextStrong,
                        )
                    }
                }
            }
        }
    }
}
