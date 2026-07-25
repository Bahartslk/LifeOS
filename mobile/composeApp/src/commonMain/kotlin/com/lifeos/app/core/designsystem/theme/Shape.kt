package com.lifeos.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The Stitch designs favor noticeably large corner radii — cards, images,
 * and sheets are never sharp — with pill-shaped (fully rounded) buttons and
 * chips. Radii here are deliberately larger than Material 3's own defaults
 * to match that "soft, premium" feel, per docs/06-design-system.md#components.
 *
 * Buttons/chips that need a true pill (rounded regardless of height) use
 * `RoundedCornerShape(percent = 50)` directly rather than this scale — see
 * [LifeOSShapes.pill].
 */
val LifeOSShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Fully rounded "pill" shape for buttons, chips, and badges. */
val LifeOSPillShape = RoundedCornerShape(percent = 50)

/** Rounded top corners only, for modal bottom sheets. */
val LifeOSBottomSheetShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 28.dp,
)
