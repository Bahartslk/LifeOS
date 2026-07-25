package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.PlannerCalendarDay
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Monthly Calendar" (this feature's requirement) — weekday headers plus
 * the full day grid, one [CalendarDay] cell per [PlannerCalendarMonth.days]
 * entry. [selectedDate] highlights at most one cell — see [CalendarDay]'s
 * KDoc for why only current-month cells can ever match it.
 */
@Composable
fun CalendarGrid(
    calendar: PlannerCalendarMonth,
    selectedDate: LocalDate?,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            calendar.weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Column {
            calendar.days.chunked(DAYS_PER_WEEK).forEachIndexed { weekIndex, week ->
                key(weekIndex) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            key(day.date) {
                                CalendarDay(
                                    day = day,
                                    isSelected = day.isCurrentMonth && day.date == selectedDate,
                                    onClick = onDaySelected,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val DAYS_PER_WEEK = 7

@Preview
@Composable
private fun CalendarGridPreview() {
    LifeOSTheme {
        CalendarGrid(
            calendar = PlannerCalendarMonth(
                monthLabel = "Ekim 2024",
                weekdayLabels = listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pa"),
                days = buildList {
                    add(PlannerCalendarDay(LocalDate(2024, 9, 30), isCurrentMonth = false, isToday = false))
                    for (day in 1..31) {
                        add(
                            PlannerCalendarDay(
                                date = LocalDate(2024, 10, day),
                                isCurrentMonth = true,
                                isToday = day == 24,
                                taskCount = if (day == 24) 3 else if (day == 25 || day == 28) 1 else 0,
                            ),
                        )
                    }
                    for (day in 1..3) add(PlannerCalendarDay(LocalDate(2024, 11, day), isCurrentMonth = false, isToday = false))
                },
            ),
            selectedDate = LocalDate(2024, 10, 24),
            onDaySelected = {},
        )
    }
}
