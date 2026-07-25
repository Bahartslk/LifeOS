package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lifeos.app.core.designsystem.components.AiBadge
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.components.PhotoOverlayCard
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.Trip
import com.lifeos.app.features.travel.domain.model.TripStatus
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The Hero Section (travel-details.png: "Cappadocia" back bar + the balloon
 * photo). Reuses [AppTopBar] for the back/share row and [PhotoOverlayCard]
 * for the cover image — [Trip] already carries every field this section
 * needs (destination, country, dates, countdown, AI badge), per this
 * task's "Reuse Trip wherever possible" rule; no new fields were added to
 * it for this feature.
 */
@Composable
fun HeroSection(
    trip: Trip,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppTopBar(
            title = trip.destinationCity,
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            navigationContentDescription = TravelStrings.BACK_CONTENT_DESCRIPTION,
            onNavigationClick = onBackClick,
            actions = {
                IconButton(onClick = onShareClick) {
                    AppIcon(imageVector = Icons.Filled.Share, contentDescription = TravelStrings.SHARE_CONTENT_DESCRIPTION)
                }
            },
        )
        PhotoOverlayCard(
            imageUrl = trip.coverImageUrl,
            contentDescription = trip.destinationCity,
            height = LifeOSSize.cardImageHeightLarge,
            topStartContent = if (trip.isAiOptimized) {
                { AiBadge(label = TravelStrings.AI_OPTIMIZED_BADGE, icon = Icons.Filled.AutoAwesome) }
            } else {
                null
            },
            topEndContent = trip.daysUntilStart?.let { days ->
                { StatusChip(label = TravelStrings.daysUntilLabel(days)) }
            },
            bottomContent = {
                Text(text = trip.destinationCountry, style = LifeOSTextStyles.overline, color = Color.White)
                Text(
                    text = trip.destinationCity,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                )
                Text(
                    text = trip.dateRangeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            },
        )
    }
}

@Preview
@Composable
private fun HeroSectionPreview() {
    LifeOSTheme {
        HeroSection(
            trip = Trip(
                id = "trip-cappadocia",
                destinationCity = "Kapadokya",
                destinationCountry = "Türkiye",
                dateRangeLabel = "12 Kasım · 4 Gün",
                status = TripStatus.PLANNED,
                coverImageUrl = null,
                isAiOptimized = true,
                daysUntilStart = 4,
                weatherTemperatureCelsius = 12,
                photoCount = null,
            ),
            onBackClick = {},
            onShareClick = {},
        )
    }
}
