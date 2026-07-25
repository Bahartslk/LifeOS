package com.lifeos.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation scale for standard, neutral shadows (cards, sheets, dialogs) —
 * matches the soft, diffused card shadows seen throughout every Stitch
 * screen (e.g. the floating Login card in login.png).
 */
object LifeOSElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp
}

/**
 * Primary buttons and the FAB in the Stitch designs (login.png "Login",
 * planner.png FAB) sit on a soft violet-tinted glow rather than a neutral
 * gray shadow. Standard Material `Modifier.shadow` always shadows in gray,
 * so this wraps it with a violet ambient/spot color to reproduce that.
 */
fun Modifier.lifeOSGlow(
    elevation: Dp = LifeOSElevation.level4,
    shape: Shape = RoundedCornerShape(percent = 50),
    glowColor: Color = LifeOSVioletBase,
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = glowColor.copy(alpha = 0.35f),
    spotColor = glowColor.copy(alpha = 0.45f),
)
