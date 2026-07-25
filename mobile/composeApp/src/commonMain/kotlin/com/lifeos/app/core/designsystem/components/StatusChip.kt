package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * Small tonal label chip — trip status, task priority/category (home.png's
 * "HIGH PRIORITY", "WORK", "PERSONAL" tags on the Today's Priority cards).
 * Deliberately takes a plain [label] and explicit colors rather than a
 * feature-specific enum (e.g. TripStatus), so this component has no
 * dependency on any feature's domain model — callers map their own status
 * to a container/content color, defaulting to a neutral tone.
 */
@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = contentColor,
        modifier = modifier
            .clip(LifeOSPillShape)
            .background(containerColor)
            .padding(horizontal = LifeOSSpacing.md, vertical = LifeOSSpacing.xs),
    )
}
