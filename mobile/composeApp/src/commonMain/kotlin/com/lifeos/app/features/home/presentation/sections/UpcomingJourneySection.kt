package com.lifeos.app.features.home.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppSecondaryButton
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.PhotoOverlayCard
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.home.domain.model.UpcomingJourney
import com.lifeos.app.features.home.presentation.HomeStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Your Next Adventure" (home.png): a cover-photo hero card with flight and
 * hotel details and a countdown. Falls back to [com.lifeos.app.core.designsystem.components.EmptyState]
 * when [journey] is `null` — no upcoming trip is a real, expected state
 * (FR-HOME-02), not an error.
 */
@Composable
fun UpcomingJourneySection(
    journey: UpcomingJourney?,
    onViewMapClick: () -> Unit,
    onPackingListClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = HomeStrings.JOURNEY_TITLE, style = MaterialTheme.typography.titleLarge)
            // A countdown badge, not a tappable action — deliberately not
            // SectionHeader's actionLabel slot, which implies a click handler.
            if (journey != null) {
                StatusChip(label = HomeStrings.journeyDaysRemainingLabel(journey.daysRemaining))
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))

        if (journey == null) {
            EmptyState(
                title = HomeStrings.JOURNEY_EMPTY_TITLE,
                description = HomeStrings.JOURNEY_EMPTY_DESCRIPTION,
                icon = Icons.Filled.FlightTakeoff,
                bordered = true,
                action = {
                    AppSecondaryButton(text = HomeStrings.QUICK_ACTION_CREATE_TRIP, onClick = onCreateTripClick)
                },
            )
        } else {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp),
            ) {
                JourneyCoverImage(journey = journey)
                Column(modifier = Modifier.padding(LifeOSSpacing.lg)) {
                    JourneyDetailsRow(journey = journey)
                    Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                    AppSecondaryButton(text = HomeStrings.JOURNEY_VIEW_MAP, onClick = onViewMapClick)
                    Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
                    AppOutlinedButton(text = HomeStrings.JOURNEY_PACKING_LIST, onClick = onPackingListClick)
                }
            }
        }
    }
}

@Composable
private fun JourneyCoverImage(journey: UpcomingJourney) {
    PhotoOverlayCard(
        imageUrl = journey.coverImageUrl,
        contentDescription = journey.destinationName,
        // No weather chip at all when there's no real weather data (Travel's
        // backend has none) — an honest omission, not a placeholder reading.
        topEndContent = journey.weatherTemperatureCelsius?.let { celsius ->
            {
                StatusChip(
                    label = HomeStrings.journeyWeatherLabel(celsius),
                    containerColor = Color.White.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        bottomContent = {
            Text(text = journey.region, style = LifeOSTextStyles.overline, color = Color.White)
            Text(
                text = journey.destinationName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
        },
    )
}

/**
 * Renders the flight column, the hotel column, both, or neither — Travel's
 * backend never supplies flight data and only sometimes supplies hotel data
 * (see [UpcomingJourney]'s KDoc), so this row is not guaranteed to have
 * anything to show at all. Renders nothing rather than an empty/broken-
 * looking `Row` when both are absent.
 */
@Composable
private fun JourneyDetailsRow(journey: UpcomingJourney) {
    val hasFlight = journey.flightCode != null || journey.flightGate != null || journey.flightDepartureLabel != null
    val hasHotel = journey.hotelName != null || journey.hotelRoomType != null
    if (!hasFlight && !hasHotel) return

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (hasFlight) {
            JourneyDetailColumn(
                icon = Icons.Filled.FlightTakeoff,
                label = HomeStrings.JOURNEY_FLIGHT_LABEL,
                primaryText = listOfNotNull(journey.flightCode, journey.flightGate).joinToString(" · "),
                secondaryText = journey.flightDepartureLabel.orEmpty(),
            )
        }
        if (hasHotel) {
            JourneyDetailColumn(
                icon = Icons.Filled.Hotel,
                label = HomeStrings.JOURNEY_STAY_LABEL,
                primaryText = journey.hotelName.orEmpty(),
                secondaryText = journey.hotelRoomType.orEmpty(),
            )
        }
    }
}

@Composable
private fun JourneyDetailColumn(
    icon: ImageVector,
    label: String,
    primaryText: String,
    secondaryText: String,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(imageVector = icon, contentDescription = null, size = LifeOSSize.iconSmall)
            Spacer(modifier = Modifier.width(LifeOSSpacing.xs))
            Text(text = label, style = LifeOSTextStyles.overline)
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        Text(text = primaryText, style = MaterialTheme.typography.titleSmall)
        Text(
            text = secondaryText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun UpcomingJourneySectionPreview() {
    LifeOSTheme {
        UpcomingJourneySection(
            journey = UpcomingJourney(
                destinationName = "Kapadokya",
                region = "Türkiye",
                daysRemaining = 3,
                weatherTemperatureCelsius = 22,
                flightCode = "AF084",
                flightGate = "L2",
                flightDepartureLabel = "10:45 Kalkış",
                hotelName = "Museum Hotel",
                hotelRoomType = "Deluxe Mağara Süiti",
                coverImageUrl = null,
            ),
            onViewMapClick = {},
            onPackingListClick = {},
            onCreateTripClick = {},
        )
    }
}

@Preview
@Composable
private fun UpcomingJourneySectionEmptyPreview() {
    LifeOSTheme {
        UpcomingJourneySection(
            journey = null,
            onViewMapClick = {},
            onPackingListClick = {},
            onCreateTripClick = {},
        )
    }
}
