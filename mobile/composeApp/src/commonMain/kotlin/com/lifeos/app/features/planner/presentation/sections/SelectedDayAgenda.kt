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
import com.lifeos.app.core.designsystem.components.SkeletonCard
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
 * "Daily Agenda" + "Empty Day State" (this feature's requirement) — the
 * selected day's tasks, or [EmptyState] when it has none. Renders
 * [AgendaTaskCard] per task rather than owning any row layout itself
 * (single responsibility: section chrome + state switching only).
 */
@Composable
fun SelectedDayAgenda(
    dayTitle: String,
    tasks: List<Task>,
    isLoading: Boolean,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = dayTitle)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        when {
            isLoading -> Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                repeat(AGENDA_LOADING_SKELETON_COUNT) { SkeletonCard() }
            }
            tasks.isEmpty() -> EmptyState(
                title = PlannerStrings.CALENDAR_AGENDA_EMPTY_TITLE,
                description = PlannerStrings.CALENDAR_AGENDA_EMPTY_DESCRIPTION,
                icon = Icons.Filled.EventAvailable,
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                tasks.forEach { task ->
                    key(task.id) {
                        AgendaTaskCard(task = task, onClick = { onTaskClick(task.id) })
                    }
                }
            }
        }
    }
}

private const val AGENDA_LOADING_SKELETON_COUNT = 2

@Preview
@Composable
private fun SelectedDayAgendaLoadingPreview() {
    LifeOSTheme {
        SelectedDayAgenda(dayTitle = "24 Ekim", tasks = emptyList(), isLoading = true, onTaskClick = {})
    }
}

@Preview
@Composable
private fun SelectedDayAgendaEmptyPreview() {
    LifeOSTheme {
        SelectedDayAgenda(dayTitle = "26 Ekim", tasks = emptyList(), isLoading = false, onTaskClick = {})
    }
}

@Preview
@Composable
private fun SelectedDayAgendaWithTasksPreview() {
    LifeOSTheme {
        SelectedDayAgenda(
            dayTitle = "24 Ekim",
            tasks = listOf(
                Task(
                    id = "task-project-work",
                    title = "Proje Çalışması",
                    description = null,
                    dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(15, 30)),
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.WORK,
                    status = TaskStatus.TODO,
                    source = TaskSource.PLANNER,
                    tags = emptyList(),
                    hasReminder = true,
                    createdAt = LocalDate(2024, 10, 18),
                ),
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
            ),
            isLoading = false,
            onTaskClick = {},
        )
    }
}
