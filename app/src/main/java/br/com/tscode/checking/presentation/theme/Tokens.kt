package br.com.tscode.checking.presentation.theme

import androidx.compose.ui.unit.dp

// Design tokens extracted from styles.css (§14.2).
// Named after their CSS custom-property counterparts for easy cross-referencing.

object Tokens {
    // Layout
    val headerHeight = 64.dp
    val cardMaxWidth = 680.dp
    val cardRadius = 16.dp
    val cardRadiusLarge = 22.dp
    val controlHeight = 40.dp
    val controlRadius = 12.dp
    val controlRadiusLarge = 14.dp

    // Spacing
    val sectionGap = 12.dp
    val sectionGapLarge = 16.dp
    val itemGap = 8.dp
    val cardPadding = 20.dp
    val cardPaddingSmall = 16.dp
    val inputPaddingHorizontal = 14.dp
    val inputPaddingVertical = 12.dp
    val buttonPaddingHorizontal = 20.dp
    val buttonPaddingVertical = 12.dp

    // Elevation
    val cardElevation = 8.dp
    val dialogElevation = 8.dp

    // Icon sizes
    val iconDefault = 24.dp
    val iconSmall = 20.dp
    val iconLarge = 28.dp
}
