package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The screen header (planner.png: small "LifeOS" brand row with a settings
 * shortcut, then "Ajandam" + today's date, then the AI subtitle). Composed
 * from plain [Text]/[AppIcon] rather than [com.lifeos.app.core.designsystem.components.AppTopBar] —
 * unlike every other feature's header, Planner's has two distinct title
 * tiers (a small brand row, then a large screen title), which [AppTopBar]'s
 * single-title shape doesn't fit.
 */
@Composable
fun PlannerHeader(
    dateLabel: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = LifeOSSpacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    imageVector = Icons.Filled.GridView,
                    contentDescription = null,
                    tint = LifeOSVioletBase,
                    size = LifeOSSize.iconSmall,
                )
                Spacer(modifier = Modifier.width(LifeOSSpacing.xs))
                Text(
                    text = PlannerStrings.BRAND_NAME,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onSettingsClick) {
                AppIcon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = PlannerStrings.SETTINGS_CONTENT_DESCRIPTION,
                )
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = PlannerStrings.TITLE, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        Text(
            text = PlannerStrings.SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun PlannerHeaderPreview() {
    LifeOSTheme {
        PlannerHeader(dateLabel = "24 Ekim Perşembe", onSettingsClick = {})
    }
}
