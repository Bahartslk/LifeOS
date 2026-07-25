package com.lifeos.app.features.travel.domain.model

/**
 * One day of the "Weather Forecast" (this task's requirement; the AI
 * Forecast card's single "Weather: 12°C" chip is the only weather info in
 * the Stitch mockup itself — see this feature's "Deviations from Stitch"
 * note for the multi-day forecast this section adds).
 */
data class DailyWeather(
    val dayLabel: String,
    val temperatureCelsius: Int,
    val condition: WeatherCondition,
)

enum class WeatherCondition {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY,
    WINDY,
}
