package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
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
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Gecikmiş Görevler" — the dashboard's `overdueTasks` (unfinished tasks
 * due before today), each a [TaskCard] marked overdue and still
 * completable in place, the same way [TodayTasksSection] is. Has no empty
 * state of its own: the caller simply doesn't render it when [tasks] is
 * empty, so a user with nothing overdue never sees an extra section.
 */
@Composable
fun OverdueTasksSection(
    tasks: List<Task>,
    onTaskToggled: (String) -> Unit,
    onTaskClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = PlannerStrings.OVERDUE_TASKS_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
            tasks.forEach { task ->
                key(task.id) {
                    TaskCard(
                        task = task,
                        onToggle = { onTaskToggled(task.id) },
                        onClick = { onTaskClicked(task.id) },
                        isOverdue = true,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun OverdueTasksSectionPreview() {
    LifeOSTheme {
        OverdueTasksSection(
            tasks = listOf(
                Task(
                    id = "task-overdue-invoice",
                    title = "Fatura Ödemesi",
                    description = null,
                    dueDate = TaskDueDate(date = LocalDate(2024, 10, 21), time = null),
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.FINANCE,
                    status = TaskStatus.TODO,
                    source = TaskSource.PLANNER,
                    tags = emptyList(),
                    hasReminder = false,
                    createdAt = LocalDate(2024, 10, 15),
                ),
            ),
            onTaskToggled = {},
            onTaskClicked = {},
        )
    }
}
