package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.SectionHeader
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
 * "Günlük Akış" (Today's Tasks; planner.png) — the day's timeline, one
 * [TaskCard] per task.
 */
@Composable
fun TodayTasksSection(
    tasks: List<Task>,
    onTaskToggled: (String) -> Unit,
    onTaskClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = PlannerStrings.TODAY_TASKS_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        if (tasks.isEmpty()) {
            EmptyState(
                title = PlannerStrings.TODAY_TASKS_EMPTY_TITLE,
                description = PlannerStrings.TODAY_TASKS_EMPTY_DESCRIPTION,
                icon = Icons.Filled.EventAvailable,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                tasks.forEach { task ->
                    key(task.id) {
                        TaskCard(
                            task = task,
                            onToggle = { onTaskToggled(task.id) },
                            onClick = { onTaskClicked(task.id) },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TodayTasksSectionPreview() {
    LifeOSTheme {
        TodayTasksSection(
            tasks = listOf(
                Task(
                    id = "task-morning-routine",
                    title = "Sabah Rutini",
                    description = null,
                    dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(8, 30)),
                    priority = TaskPriority.LOW,
                    category = TaskCategory.PERSONAL,
                    status = TaskStatus.DONE,
                    source = TaskSource.PLANNER,
                    tags = emptyList(),
                    hasReminder = false,
                    createdAt = LocalDate(2024, 10, 20),
                ),
                Task(
                    id = "task-team-meeting",
                    title = "Ekip Toplantısı",
                    description = null,
                    dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(10, 0)),
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.WORK,
                    status = TaskStatus.IN_PROGRESS,
                    source = TaskSource.PLANNER,
                    tags = listOf("Ayşe", "Mehmet", "Zeynep", "Can"),
                    hasReminder = true,
                    createdAt = LocalDate(2024, 10, 21),
                ),
            ),
            onTaskToggled = {},
            onTaskClicked = {},
        )
    }
}

@Preview
@Composable
private fun TodayTasksSectionEmptyPreview() {
    LifeOSTheme {
        TodayTasksSection(tasks = emptyList(), onTaskToggled = {}, onTaskClicked = {})
    }
}
