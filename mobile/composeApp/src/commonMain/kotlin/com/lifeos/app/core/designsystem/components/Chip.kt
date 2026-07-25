package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles

/**
 * The uppercase, wide-tracking "eyebrow" pill seen above headlines —
 * "EXECUTIVE FUNCTION HUB" (onboarding-1.png), "READY TO LAUNCH"
 * (onboarding-3.png), "AI TRAVEL PLANNER" (create-travel.png). Purely
 * informational, never tappable.
 */
@Composable
fun EyebrowChip(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(LifeOSPillShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = LifeOSSpacing.md, vertical = LifeOSSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            AppIcon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                size = LifeOSSize.iconSmall,
                modifier = Modifier.padding(end = LifeOSSpacing.xs),
            )
        }
        Text(
            text = label,
            style = LifeOSTextStyles.overline,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * A tappable outlined suggestion pill — the follow-up prompts under the AI
 * Assistant's reply in create-travel.png ("Add a stop in Istanbul?", "Show
 * cave hotels", "Adjust budget to 30,000 TL").
 *
 * [selected] toggles it into a solid tonal fill with primary-colored text —
 * reused as-is (rather than a new component) for "Create Travel (AI)"'s
 * single-choice option groups (Travel Style, Companions, Transportation,
 * Accommodation Preference): the exact same tappable-pill shape, just with
 * a persistent chosen/unchosen state instead of firing once and
 * disappearing.
 */
@Composable
fun SuggestionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = contentColor,
        modifier = modifier
            .clip(LifeOSPillShape)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .border(width = 1.dp, color = borderColor, shape = LifeOSPillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = LifeOSSpacing.lg, vertical = LifeOSSpacing.md),
    )
}
