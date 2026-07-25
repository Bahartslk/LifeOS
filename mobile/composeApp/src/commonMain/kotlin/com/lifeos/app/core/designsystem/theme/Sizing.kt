package com.lifeos.app.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fixed sizes for components, as distinct from [LifeOSSpacing] (the gaps
 * *between* things). Every reusable component in `core/designsystem/components`
 * should size itself from here rather than hardcoding a `Dp` literal, so a
 * single change (e.g. a taller button for better touch targets) propagates
 * everywhere per docs/06-design-system.md#components ("Consistent component
 * sizes").
 */
object LifeOSSize {
    // Icons
    val iconSmall: Dp = 16.dp
    val iconMedium: Dp = 24.dp
    val iconLarge: Dp = 32.dp

    // Avatars (profile.png circular avatar, top bar avatars)
    val avatarSmall: Dp = 32.dp
    val avatarMedium: Dp = 40.dp
    val avatarLarge: Dp = 96.dp

    // Buttons / inputs — buttons are tall and pill-shaped in every Stitch screen
    val buttonHeight: Dp = 56.dp
    val buttonHeightCompact: Dp = 44.dp
    val textFieldHeight: Dp = 56.dp

    // Navigation
    val topBarHeight: Dp = 64.dp
    val bottomNavHeight: Dp = 72.dp
    val bottomNavIndicatorSize: Dp = 40.dp

    // Floating Action Button
    val fabSize: Dp = 56.dp
    val fabSizeLarge: Dp = 64.dp

    // Card imagery (travel-list.png, travel-details.png hero images)
    val cardImageHeightSmall: Dp = 120.dp
    val cardImageHeightMedium: Dp = 180.dp
    val cardImageHeightLarge: Dp = 240.dp

    // Chips / badges
    val chipHeight: Dp = 32.dp
    val badgeHeight: Dp = 22.dp

    // Skeleton loader
    val skeletonLineHeight: Dp = 14.dp
}
