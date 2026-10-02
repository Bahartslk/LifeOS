package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.PlannerCalendarDay
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The month calendar widget (planner.png: "Ekim 2024" with weekday headers
 * and a day grid, "24" highlighted as today). Stitch's own screenshot
 * appears to show only two non-contiguous weeks (30 Sep–6 Oct, then
 * 21–27 Oct, skipping 7–20) — almost certainly a mockup export artifact,
 * since a calendar widget with missing weeks would be a functional defect,
 * not a faithful reproduction. [calendar] is built as a complete,
 * calendrically-correct month grid instead (see
 * [com.lifeos.app.features.planner.domain.util.CalendarMonthBuilder]); its
 * per-day task counts come from the user's real tasks for the current month
 * (`PlannerRepositoryImpl.getDashboard`), or stay 0 if that request failed.
 *
 * This widget stays read-only and always shows the current month — it never
 * re-fetches a different month itself. [onPreviousMonthClick]/[onNextMonthClick] instead
 * open the full Planner Calendar screen (`CalendarRoute`), where
 * [MonthSelector] and [CalendarGrid] page real months — tapping either
 * chevron here is simply the fastest way in, per this app's "reuse before
 * creating new UI" rule (`PlannerViewModel` wires this).
 */
@Composable
fun PlannerCalendarSection(
    calendar: PlannerCalendarMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = calendar.monthLabel, style = MaterialTheme.typography.titleMedium)
            Row {
                IconButton(onClick = onPreviousMonthClick) {
                    AppIcon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = PlannerStrings.CALENDAR_PREVIOUS_MONTH_DESCRIPTION,
                    )
                }
                IconButton(onClick = onNextMonthClick) {
                    AppIcon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = PlannerStrings.CALENDAR_NEXT_MONTH_DESCRIPTION,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
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
            calendar.days.chunked(DAYS_PER_WEEK).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day -> CalendarDayCell(day = day, modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(day: PlannerCalendarDay, modifier: Modifier = Modifier) {
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Box(
            modifier = if (day.isToday) {
                Modifier.size(TODAY_INDICATOR_SIZE).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
            } else {
                Modifier
            },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    day.isToday -> MaterialTheme.colorScheme.onPrimary
                    !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DIMMED_ALPHA)
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

private const val DAYS_PER_WEEK = 7
private const val DIMMED_ALPHA = 0.4f
private val TODAY_INDICATOR_SIZE = 32.dp

@Preview
@Composable
private fun PlannerCalendarSectionPreview() {
    LifeOSTheme {
        PlannerCalendarSection(
            calendar = PlannerCalendarMonth(
                monthLabel = "Ekim 2024",
                weekdayLabels = listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pa"),
                days = buildList {
                    add(PlannerCalendarDay(LocalDate(2024, 9, 30), isCurrentMonth = false, isToday = false))
                    for (day in 1..31) {
                        add(PlannerCalendarDay(LocalDate(2024, 10, day), isCurrentMonth = true, isToday = day == 24))
                    }
                    for (day in 1..3) add(PlannerCalendarDay(LocalDate(2024, 11, day), isCurrentMonth = false, isToday = false))
                },
            ),
            onPreviousMonthClick = {},
            onNextMonthClick = {},
        )
    }
}
