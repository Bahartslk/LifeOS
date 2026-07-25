package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.DailyWeather
import com.lifeos.app.features.travel.domain.model.WeatherCondition
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Weather Forecast" (this task's requirement — the AI Forecast card's
 * single "Weather: 12°C" chip is all travel-details.png itself shows; see
 * this feature's "Deviations from Stitch" note for this multi-day view).
 */
@Composable
fun WeatherSection(
    forecast: List<DailyWeather>,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.WEATHER_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            forecast.forEach { day -> DailyWeatherTile(day) }
        }
    }
}

@Composable
private fun DailyWeatherTile(day: DailyWeather) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = day.dayLabel, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        AppIcon(
            imageVector = day.condition.toIcon(),
            contentDescription = null,
            size = LifeOSSize.iconLarge,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(text = "${day.temperatureCelsius}°", style = MaterialTheme.typography.titleMedium)
    }
}

private fun WeatherCondition.toIcon(): ImageVector = when (this) {
    WeatherCondition.SUNNY -> Icons.Filled.WbSunny
    WeatherCondition.CLOUDY -> Icons.Filled.Cloud
    WeatherCondition.RAINY -> Icons.Filled.Umbrella
    WeatherCondition.SNOWY -> Icons.Filled.AcUnit
    WeatherCondition.WINDY -> Icons.Filled.Air
}

@Preview
@Composable
private fun WeatherSectionPreview() {
    LifeOSTheme {
        WeatherSection(
            forecast = listOf(
                DailyWeather("Bugün", 12, WeatherCondition.SUNNY),
                DailyWeather("Yarın", 14, WeatherCondition.SUNNY),
                DailyWeather("Çar", 10, WeatherCondition.CLOUDY),
                DailyWeather("Per", 9, WeatherCondition.WINDY),
                DailyWeather("Cum", 11, WeatherCondition.SUNNY),
            ),
        )
    }
}
