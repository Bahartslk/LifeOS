package com.lifeos.app.features.home.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppSecondaryButton
import com.lifeos.app.core.designsystem.components.GradientCard
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.home.domain.model.HubHighlight
import com.lifeos.app.features.home.domain.model.HubHighlightType
import com.lifeos.app.features.home.presentation.HomeStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The "Intelligent Hub" hero card (home.png) — the one place on Home that
 * reuses [GradientCard] rather than [com.lifeos.app.core.designsystem.components.AppCard],
 * per docs/06-design-system.md#components ("hero AI-powered surfaces").
 */
@Composable
fun IntelligentHubCard(
    highlights: List<HubHighlight>,
    onCompleteChecklistClick: () -> Unit,
    onViewTripClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GradientCard(modifier = modifier.fillMaxWidth()) {
        Text(text = HomeStrings.HUB_EYEBROW, style = LifeOSTextStyles.overline)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(text = HomeStrings.HUB_TITLE, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
            highlights.forEach { highlight -> HighlightRow(highlight) }
        }

        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppSecondaryButton(
            text = HomeStrings.HUB_COMPLETE_CHECKLIST,
            onClick = onCompleteChecklistClick,
            onGradient = true,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        AppOutlinedButton(
            text = HomeStrings.HUB_VIEW_TRIP,
            onClick = onViewTripClick,
            onGradient = true,
        )
    }
}

@Composable
private fun HighlightRow(highlight: HubHighlight) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIcon(
            imageVector = highlight.type.toIcon(),
            contentDescription = null,
            tint = Color.White,
            size = LifeOSSize.iconSmall,
        )
        Spacer(modifier = Modifier.width(LifeOSSpacing.sm))
        Text(
            text = highlight.message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
    }
}

private fun HubHighlightType.toIcon(): ImageVector = when (this) {
    HubHighlightType.TRIP -> Icons.Filled.FlightTakeoff
    HubHighlightType.PACKING -> Icons.Filled.Checklist
    HubHighlightType.TASK -> Icons.Filled.PriorityHigh
    HubHighlightType.WEATHER -> Icons.Filled.WbSunny
}

@Preview
@Composable
private fun IntelligentHubCardPreview() {
    LifeOSTheme {
        IntelligentHubCard(
            highlights = listOf(
                HubHighlight(HubHighlightType.TRIP, "Seyahatiniz 3 gün sonra başlıyor"),
                HubHighlight(HubHighlightType.PACKING, "Paketleme listeniz %80 tamamlandı"),
                HubHighlight(HubHighlightType.TASK, "Bugün için 2 önemli göreviniz var"),
                HubHighlight(HubHighlightType.WEATHER, "Açık hava aktiviteleri için hava mükemmel görünüyor"),
            ),
            onCompleteChecklistClick = {},
            onViewTripClick = {},
        )
    }
}
