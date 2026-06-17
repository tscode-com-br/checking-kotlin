package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingChoiceSelectedBg
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.TransportChoiceBgEnd
import br.com.tscode.checking.presentation.theme.TransportChoiceBgStart
import br.com.tscode.checking.presentation.theme.TransportChoiceBorder
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun ChoiceCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Tokens.controlRadius)
    Surface(
        modifier = modifier
            .height(Tokens.controlHeight)
            .then(
                // Selected cards lift slightly with a soft teal shadow (CSS: translateY(-1px) + box-shadow).
                if (selected) Modifier.shadow(8.dp, shape, clip = false, spotColor = CheckingPrimary) else Modifier,
            )
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) CheckingChoiceSelectedBg else CheckingCardBg,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) CheckingPrimary else CheckingInputBorder,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) CheckingPrimary else CheckingTextStrong,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

/**
 * Transport ("Em Teste") choice — a blue gradient button matching `.transport-choice-button`.
 */
@Composable
fun TransportChoiceCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Tokens.controlRadius)
    Box(
        modifier = modifier
            .height(Tokens.controlHeight)
            .shadow(10.dp, shape, clip = false, spotColor = TransportChoiceBgEnd)
            .clip(shape)
            .background(Brush.linearGradient(listOf(TransportChoiceBgStart, TransportChoiceBgEnd)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = androidx.compose.ui.graphics.Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
fun ChoiceGrid(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        content = content,
    )
}

@Composable
fun RowScope.ChoiceGridItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    weight: Float = 1f,
    spacerAfter: Boolean = false,
) {
    ChoiceCard(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = Modifier.weight(weight),
    )
    if (spacerAfter) Spacer(modifier = Modifier.width(8.dp))
}
