package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppSecondaryButton
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.DayPeriod
import com.lifeos.app.features.travel.domain.model.ItineraryActivity
import com.lifeos.app.features.travel.domain.model.ItineraryDay
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * One "Daily Journey" row (travel-details.png: the numbered circle +
 * title + description + thumbnails / "Manage Booking"). Collapsed, it
 * shows the same single description the mockup does; expanded, it reveals
 * the Morning/Afternoon/Evening breakdown this task's Timeline Section
 * requirement adds (see this feature's "Deviations from Stitch" note).
 */
@Composable
fun TimelineDayCard(
    day: ItineraryDay,
    isLastDay: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onManageBookingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        DayIndicator(dayNumber = day.dayNumber, showTrailingLine = !isLastDay)
        Spacer(modifier = Modifier.width(LifeOSSpacing.md))
        Column(modifier = Modifier.weight(1f).padding(bottom = LifeOSSpacing.xl)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = day.title, style = MaterialTheme.typography.titleMedium)
                    if (!isExpanded) {
                        day.activities.firstOrNull()?.let { firstActivity ->
                            Text(
                                text = firstActivity.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                IconButton(onClick = onExpandToggle) {
                    AppIcon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) {
                            TravelStrings.COLLAPSE_DAY_DESCRIPTION
                        } else {
                            TravelStrings.EXPAND_DAY_DESCRIPTION
                        },
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
                Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs)) {
                    day.activities.forEach { activity -> ActivityRow(activity) }
                }
            }

            if (day.photoUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(LifeOSSpacing.md))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                    items(day.photoUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = day.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(DAY_THUMBNAIL_SIZE)
                                .clip(LifeOSShapes.small),
                        )
                    }
                }
            }

            if (day.bookingActionLabel != null) {
                Spacer(modifier = Modifier.height(LifeOSSpacing.md))
                AppSecondaryButton(text = day.bookingActionLabel, onClick = onManageBookingClick)
            }
        }
    }
}

@Composable
private fun DayIndicator(dayNumber: Int, showTrailingLine: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
        Box(
            modifier = Modifier
                .size(DAY_INDICATOR_SIZE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dayNumber.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        if (showTrailingLine) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(DAY_LINE_WIDTH)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@Composable
private fun ActivityRow(activity: ItineraryActivity) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = activity.period.label(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(ACTIVITY_LABEL_WIDTH),
        )
        Text(
            text = activity.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun DayPeriod.label(): String = when (this) {
    DayPeriod.MORNING -> TravelStrings.PERIOD_MORNING
    DayPeriod.AFTERNOON -> TravelStrings.PERIOD_AFTERNOON
    DayPeriod.EVENING -> TravelStrings.PERIOD_EVENING
}

private val DAY_INDICATOR_SIZE = 32.dp
private val DAY_LINE_WIDTH = 2.dp
private val DAY_THUMBNAIL_SIZE = 72.dp
private val ACTIVITY_LABEL_WIDTH = 96.dp

@Preview
@Composable
private fun TimelineDayCardCollapsedPreview() {
    LifeOSTheme {
        TimelineDayCard(
            day = ItineraryDay(
                dayNumber = 1,
                title = "Varış ve Mağara Otele Giriş",
                activities = listOf(
                    ItineraryActivity(DayPeriod.MORNING, "Kapadokya'ya varış ve otele transfer"),
                    ItineraryActivity(DayPeriod.AFTERNOON, "Göreme'deki mağara süitinize yerleşin"),
                    ItineraryActivity(DayPeriod.EVENING, "Antik kasaba merkezinde akşam yürüyüşü"),
                ),
                photoUrls = emptyList(),
                bookingActionLabel = null,
            ),
            isLastDay = false,
            isExpanded = false,
            onExpandToggle = {},
            onManageBookingClick = {},
        )
    }
}

@Preview
@Composable
private fun TimelineDayCardExpandedPreview() {
    LifeOSTheme {
        TimelineDayCard(
            day = ItineraryDay(
                dayNumber = 2,
                title = "Gün Doğumu Uçuşu ve Göreme Vadisi",
                activities = listOf(
                    ItineraryActivity(DayPeriod.MORNING, "Muhteşem bir saatlik balon uçuşu"),
                    ItineraryActivity(DayPeriod.AFTERNOON, "Açık Hava Müzesi ziyareti"),
                    ItineraryActivity(DayPeriod.EVENING, "Aşk Vadisi'nde yürüyüş"),
                ),
                photoUrls = emptyList(),
                bookingActionLabel = "Rezervasyonu Yönet",
            ),
            isLastDay = true,
            isExpanded = true,
            onExpandToggle = {},
            onManageBookingClick = {},
        )
    }
}
