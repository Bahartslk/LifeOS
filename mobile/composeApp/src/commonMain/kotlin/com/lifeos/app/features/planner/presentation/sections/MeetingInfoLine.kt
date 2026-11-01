package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.model.TaskType
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Toplantı · Ofis" under a meeting's title in the agenda lists. The word
 * is spelled out (not just the icon), so screen readers and color-blind
 * users get the same information; the icon is decorative.
 */
@Composable
fun MeetingInfoLine(task: Task, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AppIcon(
            imageVector = Icons.Filled.Groups,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            size = LifeOSSize.iconSmall,
        )
        Spacer(modifier = Modifier.width(LifeOSSpacing.xs))
        Text(
            text = PlannerStrings.meetingLine(task.location),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun MeetingInfoLinePreview() {
    LifeOSTheme {
        MeetingInfoLine(
            task = Task(
                id = "meeting",
                title = "Sprint planlama",
                description = null,
                dueDate = TaskDueDate(date = LocalDate(2026, 11, 2), time = LocalTime(10, 0)),
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK,
                status = TaskStatus.TODO,
                source = TaskSource.PLANNER,
                tags = emptyList(),
                createdAt = LocalDate(2026, 11, 1),
                type = TaskType.MEETING,
                durationMinutes = 60,
                location = "Ofis",
                reminderMinutesBefore = 10,
            ),
        )
    }
}
