package br.com.tscode.checking.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.tscode.checking.presentation.theme.CheckingCardBg
import br.com.tscode.checking.presentation.theme.CheckingFieldAuthedBorder
import br.com.tscode.checking.presentation.theme.CheckingFieldAuthedGlow
import br.com.tscode.checking.presentation.theme.CheckingFieldPendingBorder
import br.com.tscode.checking.presentation.theme.CheckingFieldPendingGlow
import br.com.tscode.checking.presentation.theme.CheckingInputBorder
import br.com.tscode.checking.presentation.theme.CheckingPrimary
import br.com.tscode.checking.presentation.theme.CheckingTextMutedSoft
import br.com.tscode.checking.presentation.theme.CheckingTextStrong
import br.com.tscode.checking.presentation.theme.Tokens

/**
 * Auth-field glow state — mirrors the `.auth-field-pending` / `.auth-field-authenticated`
 * CSS classes that paint a colored "brilho" around the chave/senha inputs.
 */
enum class FieldGlow { None, Pending, Authenticated }

/**
 * A text field styled like the web `.check-field`: a bold label sitting ABOVE the input
 * (not a Material floating label), a white box with a 1px slate border and `--control-radius`
 * corners, teal focus accent, and an optional colored outer glow.
 */
@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    glow: FieldGlow = FieldGlow.None,
    placeholder: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    textStyle: TextStyle? = null,
) {
    val shape = RoundedCornerShape(Tokens.controlRadius)
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val borderColor = when (glow) {
        FieldGlow.Pending -> CheckingFieldPendingBorder
        FieldGlow.Authenticated -> CheckingFieldAuthedBorder
        FieldGlow.None -> if (focused) CheckingPrimary else CheckingInputBorder
    }
    val glowColor = when (glow) {
        FieldGlow.Pending -> CheckingFieldPendingGlow
        FieldGlow.Authenticated -> CheckingFieldAuthedGlow
        FieldGlow.None -> Color.Transparent
    }
    // The CSS uses two stacked shadows (0 0 14px + 0 0 28px); a colored elevation shadow
    // is the closest native equivalent (ambient/spot tint applies on API 28+).
    val glowElevation = if (glow == FieldGlow.None) 0.dp else 16.dp
    val borderWidth = if (glow == FieldGlow.None) 1.dp else 1.5.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CheckingTextStrong,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Tokens.controlHeight)
                .shadow(
                    elevation = glowElevation,
                    shape = shape,
                    clip = false,
                    ambientColor = glowColor,
                    spotColor = glowColor,
                )
                .background(CheckingCardBg, shape)
                .border(BorderStroke(borderWidth, borderColor), shape)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                enabled = enabled,
                interactionSource = interaction,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                textStyle = (textStyle ?: MaterialTheme.typography.bodyLarge).copy(color = CheckingTextStrong),
                cursorBrush = SolidColor(CheckingPrimary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty() && !placeholder.isNullOrEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = textStyle ?: MaterialTheme.typography.bodyLarge,
                                    color = CheckingTextMutedSoft,
                                )
                            }
                            inner()
                        }
                        if (trailingIcon != null) {
                            trailingIcon()
                        }
                    }
                },
            )
        }
    }
}
