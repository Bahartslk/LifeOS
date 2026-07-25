package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppSecondaryButton
import com.lifeos.app.core.designsystem.components.GradientCard
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Progress Card" (planner.png: the violet AI-insight card — "You have a
 * busy day today..." — with its "Update Plan" button). Reuses [GradientCard]
 * + [AppSecondaryButton]'s `onGradient` variant, the exact same
 * icon-bubble-plus-message-plus-button shape
 * [com.lifeos.app.features.home.presentation.sections.IntelligentHubCard]
 * already established for "AI-authored recommendation" surfaces.
 */
@Composable
fun ProgressSection(
    message: String,
    onUpdatePlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GradientCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(LifeOSSize.avatarSmall)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = ICON_BACKGROUND_ALPHA)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = Color.White,
                    size = LifeOSSize.iconSmall,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppSecondaryButton(
            text = PlannerStrings.UPDATE_PLAN_ACTION,
            onClick = onUpdatePlanClick,
            onGradient = true,
        )
    }
}

private const val ICON_BACKGROUND_ALPHA = 0.15f

@Preview
@Composable
private fun ProgressSectionPreview() {
    LifeOSTheme {
        ProgressSection(
            message = "Bugün yoğun bir günün var. Saat 15:30'daki görevin öncesinde kısa bir mola " +
                "vermeni öneriyorum.",
            onUpdatePlanClick = {},
        )
    }
}
