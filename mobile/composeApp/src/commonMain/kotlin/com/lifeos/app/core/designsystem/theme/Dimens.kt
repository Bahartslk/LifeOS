package com.lifeos.app.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale from docs/06-design-system.md#spacing-and-layout: a 4dp base
 * unit with common steps at 8, 12, 16, 24, and 32dp. Every feature should
 * reach for these tokens instead of hardcoding dp values, so spacing stays
 * consistent as new screens are added.
 */
object LifeOSSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}
