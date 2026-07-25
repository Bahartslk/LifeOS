package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.TravelStatistics
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Quick Statistics" (this task's requirement, not literally pictured in
 * travel-list.png — see this feature's "Deviations from Stitch" note):
 * three stat tiles, following the same [AppCard] tile pattern Home's
 * Overview section already established.
 */
@Composable
fun TravelStatisticsSection(
    statistics: TravelStatistics,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
    ) {
        StatTile(
            label = TravelStrings.STAT_TOTAL_TRIPS,
            value = statistics.totalTripCount.toString(),
            modifier = Modifier.weight(1f),
        )
        StatTile(
            label = TravelStrings.STAT_COUNTRIES_VISITED,
            value = statistics.countriesVisitedCount.toString(),
            modifier = Modifier.weight(1f),
        )
        StatTile(
            label = TravelStrings.STAT_UPCOMING_TRIPS,
            value = statistics.upcomingTripCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Text(text = label, style = LifeOSTextStyles.overline)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(text = value, style = LifeOSTextStyles.statDisplay)
    }
}

@Preview
@Composable
private fun TravelStatisticsSectionPreview() {
    LifeOSTheme {
        TravelStatisticsSection(
            statistics = TravelStatistics(
                totalTripCount = 16,
                countriesVisitedCount = 8,
                upcomingTripCount = 2,
            ),
        )
    }
}
