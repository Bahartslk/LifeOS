package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * A single tappable shortcut tile: an icon in a circular tinted badge above
 * a label, inside an [AppCard] — the shape home.png's "New Task"/"Create
 * Trip"/"Ask AI"/"Notes" quick actions and planner.png's quick actions both
 * use. Originally private to Home's `QuickActionsSection`; promoted here
 * once Planner needed the identical tile shape, per this project's "only
 * extract a Design System component once it's reused by at least two
 * features" rule. Callers arrange these themselves (typically a 2-column
 * [androidx.compose.foundation.layout.Row] of two, wrapped per row) since
 * grid layout is each feature's own section concern, not this tile's.
 */
@Composable
fun QuickActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .size(LifeOSSize.avatarSmall)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    size = LifeOSSize.iconSmall,
                )
            }
            Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start,
            )
        }
    }
}
