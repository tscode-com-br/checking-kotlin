package br.com.tscode.checking.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun DialogScaffold(
    onDismiss: () -> Unit,
    // When false, tapping the scrim (outside the card) does NOT dismiss — the dialog can only be
    // closed via its own buttons. Used by the registration / set-password dialogs so an accidental
    // tap outside doesn't discard typed data. The system Back gesture still cancels (see BackHandler).
    dismissOnScrimTap: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .then(
                if (dismissOnScrimTap) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onDismiss() }
                } else {
                    Modifier
                },
            )
            // Inset the card above the on-screen keyboard so all fields and the footer buttons stay
            // reachable via the card's own vertical scroll while the IME is shown (windowSoftInputMode
            // = adjustResize + edge-to-edge feed the IME insets here).
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = Tokens.cardMaxWidth)
                .fillMaxWidth(0.92f)
                .padding(vertical = 32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { /* consume tap — prevent scrim dismissal */ },
            shape = RoundedCornerShape(Tokens.cardRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = CheckingCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = Tokens.dialogElevation),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(Tokens.sectionGap),
                content = content,
            )
        }
    }
}
