package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase
import com.lifeos.app.features.travel.domain.model.AiTripSummary
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "AI Forecast" card (travel-details.png): a fake AI-generated highlight
 * plus weather/wind chips. Reuses [AppCard] + [StatusChip] rather than
 * [com.lifeos.app.core.designsystem.components.GradientCard] — the Stitch
 * card here is white, not the violet hero-gradient surface.
 */
@Composable
fun AiSummaryCard(
    summary: AiTripSummary,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(LifeOSSize.avatarSmall)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = LifeOSVioletBase,
                    size = LifeOSSize.iconSmall,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Text(
                text = TravelStrings.AI_SUMMARY_TITLE,
                style = MaterialTheme.typography.titleMedium,
                color = LifeOSVioletBase,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Text(text = summary.highlightMessage, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
            StatusChip(label = TravelStrings.aiWeatherLabel(summary.weatherTemperatureCelsius))
            StatusChip(label = TravelStrings.aiWindLabel(summary.windSpeedKmh))
        }
    }
}

@Preview
@Composable
private fun AiSummaryCardPreview() {
    LifeOSTheme {
        AiSummaryCard(
            summary = AiTripSummary(
                highlightMessage = "En iyi balon uçuşu zamanı yarın saat 05:30. Hafif rüzgarlar, vadi " +
                    "üzerinde sakin ve manzaralı bir yolculuk için ideal koşullar sağlıyor.",
                weatherTemperatureCelsius = 12,
                windSpeedKmh = 4,
            ),
        )
    }
}
