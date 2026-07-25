package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.Trip
import com.lifeos.app.features.travel.domain.model.TripStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Upcoming Journeys" (travel-list.png): a horizontally scrolling row of
 * [TravelCard]s. No visible section title — the Stitch design flows
 * directly from [TravelHeader] into these cards.
 */
@Composable
fun UpcomingJourneysSection(
    trips: List<Trip>,
    onTripClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = LifeOSSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
    ) {
        items(trips, key = { it.id }) { trip ->
            TravelCard(
                trip = trip,
                onClick = { onTripClick(trip.id) },
                modifier = Modifier.width(UPCOMING_CARD_WIDTH),
            )
        }
    }
}

private val UPCOMING_CARD_WIDTH = 300.dp

@Preview
@Composable
private fun UpcomingJourneysSectionPreview() {
    LifeOSTheme {
        UpcomingJourneysSection(
            trips = listOf(
                Trip(
                    id = "1",
                    destinationCity = "Kyoto",
                    destinationCountry = "Japonya",
                    dateRangeLabel = "12-18 Ekim · 7 Gün",
                    status = TripStatus.PLANNED,
                    coverImageUrl = null,
                    isAiOptimized = true,
                    daysUntilStart = 14,
                    weatherTemperatureCelsius = 22,
                    photoCount = null,
                ),
                Trip(
                    id = "2",
                    destinationCity = "Ubud",
                    destinationCountry = "Endonezya",
                    dateRangeLabel = "24-30 Kasım · 6 Gün",
                    status = TripStatus.PLANNED,
                    coverImageUrl = null,
                    isAiOptimized = false,
                    daysUntilStart = 42,
                    weatherTemperatureCelsius = 28,
                    photoCount = null,
                ),
            ),
            onTripClick = {},
        )
    }
}
