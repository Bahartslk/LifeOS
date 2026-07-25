package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.DayPeriod
import com.lifeos.app.features.travel.domain.model.ItineraryActivity
import com.lifeos.app.features.travel.domain.model.ItineraryDay
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Daily Journey" (travel-details.png): the full day-by-day itinerary,
 * composed from [TimelineDayCard] rows.
 */
@Composable
fun TimelineSection(
    days: List<ItineraryDay>,
    expandedDayNumbers: Set<Int>,
    onDayExpandToggle: (Int) -> Unit,
    onManageBookingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.TIMELINE_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Column {
            days.forEachIndexed { index, day ->
                TimelineDayCard(
                    day = day,
                    isLastDay = index == days.lastIndex,
                    isExpanded = day.dayNumber in expandedDayNumbers,
                    onExpandToggle = { onDayExpandToggle(day.dayNumber) },
                    onManageBookingClick = onManageBookingClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimelineSectionPreview() {
    LifeOSTheme {
        TimelineSection(
            days = listOf(
                ItineraryDay(
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
                ItineraryDay(
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
            ),
            expandedDayNumbers = setOf(2),
            onDayExpandToggle = {},
            onManageBookingClick = {},
        )
    }
}
