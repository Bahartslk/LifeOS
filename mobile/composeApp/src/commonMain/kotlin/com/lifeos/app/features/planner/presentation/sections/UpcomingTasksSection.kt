package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Yaklaşan Önemli Tarihler" (Upcoming Tasks; planner.png) — read-only
 * cards, no completion toggle (unlike [TodayTasksSection]'s [TaskCard]s,
 * Stitch's upcoming-dates cards have no checkbox affordance at all).
 */
@Composable
fun UpcomingTasksSection(
    tasks: List<Task>,
    onTaskClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = PlannerStrings.UPCOMING_TASKS_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        if (tasks.isEmpty()) {
            EmptyState(
                title = PlannerStrings.UPCOMING_TASKS_EMPTY_TITLE,
                description = PlannerStrings.UPCOMING_TASKS_EMPTY_DESCRIPTION,
                icon = Icons.Filled.Event,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                tasks.forEach { task ->
                    key(task.id) {
                        UpcomingTaskRow(task = task, onClick = { onTaskClicked(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingTaskRow(task: Task, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(LifeOSSize.avatarSmall)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = task.category.toIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    size = LifeOSSize.iconSmall,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Column {
                Text(
                    text = task.dueDateDisplayLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = task.title, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Preview
@Composable
private fun UpcomingTasksSectionPreview() {
    LifeOSTheme {
        UpcomingTasksSection(
            tasks = listOf(
                Task(
                    id = "task-cappadocia-trip",
                    title = "Kapadokya Seyahati",
                    description = null,
                    dueDate = TaskDueDate(date = LocalDate(2024, 11, 12)),
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.TRAVEL,
                    status = TaskStatus.TODO,
                    source = TaskSource.TRAVEL,
                    tags = emptyList(),
                    hasReminder = true,
                    createdAt = LocalDate(2024, 10, 1),
                ),
                Task(
                    id = "task-doctor-appointment",
                    title = "Doktor Randevusu",
                    description = null,
                    dueDate = TaskDueDate(date = LocalDate(2024, 10, 25), time = LocalTime(14, 0)),
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.HEALTH,
                    status = TaskStatus.TODO,
                    source = TaskSource.PLANNER,
                    tags = emptyList(),
                    hasReminder = true,
                    createdAt = LocalDate(2024, 10, 15),
                ),
            ),
            onTaskClicked = {},
        )
    }
}

@Preview
@Composable
private fun UpcomingTasksSectionEmptyPreview() {
    LifeOSTheme {
        UpcomingTasksSection(tasks = emptyList(), onTaskClicked = {})
    }
}
