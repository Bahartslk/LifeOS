package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * One destination in [AppBottomNavigation]. [label] is plain text supplied
 * by the caller — this design system never hardcodes navigation labels,
 * since which five destinations exist and what they're called is an
 * application/navigation concern (docs/05-screen-inventory.md#navigation-map),
 * not a design-system one.
 */
data class AppBottomNavigationItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
)

/**
 * Bottom navigation bar matching every Stitch screen's pattern: the active
 * item's icon sits inside a filled violet pill, with violet label text;
 * inactive items are plain gray icon + label (home.png, ai-assistent.png,
 * travel-list.png, planner.png, profile.png all use this same treatment).
 */
@Composable
fun AppBottomNavigation(
    items: List<AppBottomNavigationItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LifeOSSize.bottomNavHeight)
                .padding(horizontal = LifeOSSpacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item -> BottomNavItem(item) }
        }
    }
}

@Composable
private fun BottomNavItem(item: AppBottomNavigationItem) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (item.selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = item.onClick,
            )
            .padding(horizontal = LifeOSSpacing.sm, vertical = LifeOSSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(LifeOSSize.bottomNavIndicatorSize)
                .clip(LifeOSPillShape)
                .background(
                    if (item.selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        androidx.compose.ui.graphics.Color.Transparent
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            // contentDescription is null (decorative): Modifier.clickable merges
            // descendant semantics, and the Text label below already provides
            // the accessible name — a non-null description here would cause
            // screen readers to announce the label twice.
            AppIcon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                size = LifeOSSize.iconMedium,
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}
