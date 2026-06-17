package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingCardTint
import br.com.tscode.checking.presentation.theme.CheckingTeal
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun CheckCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CheckingCardBg,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = modifier
                .widthIn(max = Tokens.cardMaxWidth)
                .fillMaxWidth(),
            shape = RoundedCornerShape(Tokens.cardRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = Tokens.cardElevation),
            border = BorderStroke(width = 1.dp, color = CheckingTeal.copy(alpha = 0.18f)),
        ) {
            Box(modifier = Modifier.padding(Tokens.cardPadding)) {
                content()
            }
        }
    }
}

/**
 * Inner tinted panel used for the history / notification / location sections.
 * Mirrors the web `.history-card` / `.notification-card` / `.location-card`:
 * a slate-tinted background with a subtle teal border and rounded corners,
 * sitting INSIDE the big white [CheckCard].
 */
@Composable
fun TintedPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Tokens.controlRadius + 2.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CheckingCardTint)
            .border(BorderStroke(1.dp, CheckingTeal.copy(alpha = 0.16f)), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        content = content,
    )
}
