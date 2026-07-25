package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.PlannerCalendarDay
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * One grid cell (this feature's requirement: "Today Highlight" + "Task
 * Count Indicators" + "Selected Day"). Only [PlannerCalendarDay.isCurrentMonth]
 * cells are selectable — a leading/trailing day borrowed from an adjacent
 * month stays purely decorative (dimmed, non-interactive), the same
 * treatment [PlannerCalendarSection]'s read-only widget already gives
 * those cells, avoiding the ambiguity of "which month am I actually
 * looking at" a tap-through would introduce.
 */
@Composable
fun CalendarDay(
    day: PlannerCalendarDay,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cellModifier = if (day.isCurrentMonth) {
        modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onClick(day.date) }
            .semantics {
                contentDescription = PlannerStrings.calendarDayContentDescription(
                    day = day.date.dayOfMonth,
                    taskCount = day.taskCount,
                    isToday = day.isToday,
                )
            }
    } else {
        modifier.aspectRatio(1f)
    }

    Box(modifier = cellModifier, contentAlignment = Alignment.Center) {
        val backgroundModifier = when {
            isSelected -> Modifier.size(DAY_INDICATOR_SIZE).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
            day.isToday -> Modifier
                .size(DAY_INDICATOR_SIZE)
                .clip(CircleShape)
                .border(width = 1.5.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            else -> Modifier
        }

        Box(modifier = backgroundModifier, contentAlignment = Alignment.Center) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    day.isToday -> MaterialTheme.colorScheme.primary
                    !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DIMMED_ALPHA)
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }

        if (day.taskCount > 0) {
            Box(
                modifier = Modifier
                    .padding(top = TASK_DOT_TOP_PADDING)
                    .align(Alignment.BottomCenter)
                    .size(TASK_DOT_SIZE)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    ),
            )
        }
    }
}

private val DAY_INDICATOR_SIZE = 32.dp
private val TASK_DOT_SIZE = 4.dp
private val TASK_DOT_TOP_PADDING = 26.dp
private const val DIMMED_ALPHA = 0.4f

@Preview
@Composable
private fun CalendarDayTodayPreview() {
    LifeOSTheme {
        CalendarDay(
            day = PlannerCalendarDay(date = LocalDate(2024, 10, 24), isCurrentMonth = true, isToday = true, taskCount = 3),
            isSelected = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun CalendarDaySelectedPreview() {
    LifeOSTheme {
        CalendarDay(
            day = PlannerCalendarDay(date = LocalDate(2024, 10, 18), isCurrentMonth = true, isToday = false, taskCount = 1),
            isSelected = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun CalendarDayOtherMonthPreview() {
    LifeOSTheme {
        CalendarDay(
            day = PlannerCalendarDay(date = LocalDate(2024, 9, 30), isCurrentMonth = false, isToday = false),
            isSelected = false,
            onClick = {},
        )
    }
}
