package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.app.core.designsystem.theme.LifeOSGradients
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.lifeOSGlow

/**
 * The floating action button, per home.png (small, corner) and
 * planner.png (larger, prominent with a visible glow). Always the violet
 * gradient — a FAB is always the single most prominent action on the
 * screen, so it always gets the brand treatment, never a plain surface
 * color.
 */
@Composable
fun AppFab(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
    large: Boolean = false,
) {
    val size = if (large) LifeOSSize.fabSizeLarge else LifeOSSize.fabSize

    Box(
        modifier = modifier
            .size(size)
            .lifeOSGlow(shape = LifeOSPillShape)
            .clip(LifeOSPillShape)
            .background(LifeOSGradients.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            size = LifeOSSize.iconMedium,
        )
    }
}
