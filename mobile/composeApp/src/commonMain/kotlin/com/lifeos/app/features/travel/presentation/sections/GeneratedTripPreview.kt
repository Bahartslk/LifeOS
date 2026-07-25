package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.data.datasource.FakeTripGenerationDataSource
import com.lifeos.app.features.travel.domain.model.AccommodationPreference
import com.lifeos.app.features.travel.domain.model.TransportationType
import com.lifeos.app.features.travel.domain.model.TravelCompanions
import com.lifeos.app.features.travel.domain.model.TravelStyle
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripGenerationRequest
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Generated Preview" (this task's screen content): the full generated
 * draft — AI summary, itinerary, weather, budget, packing checklist, and
 * travel tips — composed entirely from sections already built for Travel
 * Detail ([AiSummaryCard], [TimelineSection], [WeatherSection],
 * [BudgetSection], [PackingChecklistSection]), per this task's "reuse
 * before creating" rule. Only [TravelTipsSection] is new — Travel Detail's
 * AI Forecast card has no equivalent concept.
 *
 * A plain [Column], not a `LazyColumn` — this is one item inside
 * `CreateTripScreen`'s own `LazyColumn`, and nesting a scrollable list
 * inside another is never correct in Compose.
 */
@Composable
fun GeneratedTripPreview(
    detail: TripDetail,
    expandedDayNumbers: Set<Int>,
    onDayExpandToggle: (Int) -> Unit,
    onPackingItemToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg)) {
        Text(text = TravelStrings.PREVIEW_TITLE, style = MaterialTheme.typography.titleLarge)
        AiSummaryCard(summary = detail.aiSummary)
        TimelineSection(
            days = detail.itinerary,
            expandedDayNumbers = expandedDayNumbers,
            onDayExpandToggle = onDayExpandToggle,
            onManageBookingClick = {},
        )
        WeatherSection(forecast = detail.weatherForecast)
        BudgetSection(budget = detail.budget)
        PackingChecklistSection(
            categories = detail.packingCategories,
            onItemToggled = onPackingItemToggled,
            onViewFullListClick = {},
        )
        TravelTipsSection(tips = detail.aiSummary.travelTips)
    }
}

/** "Travel tips" (this task's requirement) — [AiTripSummary.travelTips][com.lifeos.app.features.travel.domain.model.AiTripSummary.travelTips]. */
@Composable
private fun TravelTipsSection(tips: List<String>, modifier: Modifier = Modifier) {
    if (tips.isEmpty()) return

    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.TRAVEL_TIPS_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
            tips.forEach { tip ->
                Row(verticalAlignment = Alignment.Top) {
                    AppIcon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        size = LifeOSSize.iconSmall,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(LifeOSSpacing.sm))
                    Text(text = tip, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview
@Composable
private fun GeneratedTripPreviewPreview() {
    LifeOSTheme {
        val detail = FakeTripGenerationDataSource().generateTripDetail(
            request = TripGenerationRequest(
                destinationCity = "Kyoto",
                destinationCountry = "Japonya",
                startDateLabel = "15.09.2026",
                endDateLabel = "22.09.2026",
                travelStyle = TravelStyle.ADVENTURE,
                budgetAmount = 15000,
                companions = TravelCompanions.COUPLE,
                transportation = TransportationType.FLIGHT,
                accommodationPreference = AccommodationPreference.BOUTIQUE,
                additionalNotes = "",
            ),
            tripId = "trip-preview",
        )
        GeneratedTripPreview(
            detail = detail,
            expandedDayNumbers = setOf(1),
            onDayExpandToggle = {},
            onPackingItemToggled = {},
        )
    }
}
