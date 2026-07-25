package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AiBadge
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.Badge
import com.lifeos.app.core.designsystem.components.PhotoOverlayCard
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSStatusCompleted
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.Trip
import com.lifeos.app.features.travel.domain.model.TripStatus
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A single trip card, rendering either the Upcoming or Past visual variant
 * based on [trip]'s status (travel-list.png shows two distinct layouts:
 * countdown+weather+status for planned trips, a "COMPLETED" badge+photo
 * count for finished ones). One composable with an internal branch, not two
 * near-duplicate components — the same pattern [com.lifeos.app.core.designsystem.components.AppSecondaryButton]
 * already uses for its `onGradient` variants.
 */
@Composable
fun TravelCard(
    trip: Trip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOptionsClick: (() -> Unit)? = null,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
    ) {
        when (trip.status) {
            TripStatus.COMPLETED -> PastTripContent(trip = trip, onOptionsClick = onOptionsClick)
            else -> UpcomingTripContent(trip = trip)
        }
    }
}

@Composable
private fun UpcomingTripContent(trip: Trip) {
    PhotoOverlayCard(
        imageUrl = trip.coverImageUrl,
        contentDescription = trip.destinationCity,
        topStartContent = if (trip.isAiOptimized) {
            { AiBadge(label = TravelStrings.AI_OPTIMIZED_BADGE, icon = Icons.Filled.AutoAwesome) }
        } else {
            null
        },
        topEndContent = trip.daysUntilStart?.let { days ->
            { StatusChip(label = TravelStrings.daysUntilLabel(days)) }
        },
        bottomContent = {
            Text(text = trip.dateRangeLabel, style = LifeOSTextStyles.overline, color = Color.White)
            Text(
                text = "${trip.destinationCity}, ${trip.destinationCountry}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
        },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(all = LifeOSSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
    ) {
        if (trip.weatherTemperatureCelsius != null) {
            InfoChip(
                icon = Icons.Filled.WbSunny,
                label = TravelStrings.weatherLabel(trip.weatherTemperatureCelsius),
                modifier = Modifier.weight(1f),
            )
        }
        InfoChip(
            icon = Icons.Filled.CheckCircle,
            label = TravelStrings.STATUS_CONFIRMED,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PastTripContent(trip: Trip, onOptionsClick: (() -> Unit)?) {
    PhotoOverlayCard(
        imageUrl = trip.coverImageUrl,
        contentDescription = trip.destinationCity,
        height = LifeOSSize.cardImageHeightMedium,
        topEndContent = { Badge(label = TravelStrings.COMPLETED_BADGE, containerColor = LifeOSStatusCompleted) },
        bottomContent = {
            Text(
                text = "${trip.destinationCity}, ${trip.destinationCountry}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
        },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(all = LifeOSSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = trip.dateRangeLabel, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    imageVector = Icons.Filled.Photo,
                    contentDescription = null,
                    size = LifeOSSize.iconSmall,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(LifeOSSpacing.xs))
                Text(
                    text = trip.photoCount?.let(TravelStrings::photoCountLabel).orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (onOptionsClick != null) {
            IconButton(onClick = onOptionsClick) {
                AppIcon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = TravelStrings.TRIP_OPTIONS_DESCRIPTION,
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = LifeOSShapes.medium,
            )
            .padding(horizontal = LifeOSSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs),
    ) {
        AppIcon(imageVector = icon, contentDescription = null, size = LifeOSSize.iconSmall)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview
@Composable
private fun TravelCardUpcomingPreview() {
    LifeOSTheme {
        TravelCard(
            trip = Trip(
                id = "preview-upcoming",
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
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun TravelCardPastPreview() {
    LifeOSTheme {
        TravelCard(
            trip = Trip(
                id = "preview-past",
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
            onClick = {},
            onOptionsClick = {},
        )
    }
}
