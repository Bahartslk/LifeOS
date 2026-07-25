package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.Trip
import com.lifeos.app.features.travel.domain.model.TripStatus
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Memories" (travel-list.png): completed trips, plus the dashed "Past
 * Archives" teaser card for older journeys — reusing [EmptyState]'s
 * `bordered` variant, the same dashed-card treatment Home already
 * established for its own empty state.
 */
@Composable
fun PastJourneysSection(
    trips: List<Trip>,
    archivedTripCount: Int,
    archivedYearRangeLabel: String,
    onTripClick: (String) -> Unit,
    onTripOptionsClick: (String) -> Unit,
    onViewArchivedTripsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = LifeOSSpacing.lg)) {
        SectionHeader(title = TravelStrings.PAST_SECTION_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
            trips.forEach { trip ->
                TravelCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) },
                    onOptionsClick = { onTripOptionsClick(trip.id) },
                )
            }
        }

        if (archivedTripCount > 0) {
            Spacer(modifier = Modifier.height(LifeOSSpacing.md))
            EmptyState(
                title = TravelStrings.ARCHIVE_TITLE,
                description = TravelStrings.archiveDescription(archivedTripCount, archivedYearRangeLabel),
                icon = Icons.Filled.Inventory2,
                bordered = true,
                action = {
                    AppOutlinedButton(
                        text = TravelStrings.ARCHIVE_VIEW_ACTION,
                        onClick = onViewArchivedTripsClick,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun PastJourneysSectionPreview() {
    LifeOSTheme {
        PastJourneysSection(
            trips = listOf(
                Trip(
                    id = "1",
                    destinationCity = "Paris",
                    destinationCountry = "Fransa",
                    dateRangeLabel = "05-12 Eylül · 7 Gün",
                    status = TripStatus.COMPLETED,
                    coverImageUrl = null,
                    isAiOptimized = false,
                    daysUntilStart = null,
                    weatherTemperatureCelsius = null,
                    photoCount = 428,
                ),
            ),
            archivedTripCount = 12,
            archivedYearRangeLabel = "2022-2023",
            onTripClick = {},
            onTripOptionsClick = {},
            onViewArchivedTripsClick = {},
        )
    }
}
