package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.app.core.designsystem.theme.LifeOSGradients
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase

/**
 * A small solid status badge overlaid on imagery — "COMPLETED" on a trip
 * memory photo (travel-list.png). Unlike [StatusChip] (tonal, sits inline in
 * a layout), a Badge is solid-filled and meant to be positioned on top of an
 * image or card corner.
 */
@Composable
fun Badge(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = LifeOSTealBase,
    contentColor: Color = Color.White,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        modifier = modifier
            .clip(LifeOSPillShape)
            .background(containerColor)
            .padding(horizontal = LifeOSSpacing.sm, vertical = LifeOSSpacing.xs),
    )
}

/**
 * The gradient variant with a leading icon — "AI Optimized Plan" on the
 * Kyoto trip card (travel-list.png). Always violet: this specific badge
 * means "AI touched this", so it intentionally never takes a custom color.
 */
@Composable
fun AiBadge(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(LifeOSPillShape)
            .background(LifeOSGradients.primary)
            .padding(horizontal = LifeOSSpacing.md, vertical = LifeOSSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            size = LifeOSSize.iconSmall,
            modifier = Modifier.padding(end = LifeOSSpacing.xs),
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
