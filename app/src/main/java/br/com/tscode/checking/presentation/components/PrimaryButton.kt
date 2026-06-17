package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.theme.CheckingOnPrimary
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingPrimaryDark
import br.com.tscode.checking.presentation.theme.Tokens

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val gradient = if (enabled) {
        Brush.linearGradient(colors = listOf(CheckingPrimary, CheckingPrimaryDark))
    } else {
        Brush.linearGradient(
            colors = listOf(CheckingPrimary.copy(alpha = 0.38f), CheckingPrimaryDark.copy(alpha = 0.38f)),
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(Tokens.controlHeight),
        shape = RoundedCornerShape(Tokens.controlRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = CheckingOnPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = CheckingOnPrimary.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(Tokens.controlHeight),
        shape = RoundedCornerShape(Tokens.controlRadius),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = CheckingPrimary,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (enabled) CheckingPrimary else CheckingPrimary.copy(alpha = 0.38f),
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
