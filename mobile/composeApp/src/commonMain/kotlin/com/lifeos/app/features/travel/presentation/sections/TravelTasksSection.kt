package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import com.lifeos.app.core.date.RelativeDateFormatter
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.travel.presentation.TravelStrings
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Trip Preparation Tasks" (this integration's requirement) — Planner's own
 * [Task]s tagged [TaskSource.TRAVEL], shown read-only here (tap navigates to
 * Task Detail, where completion is actually toggled — the same "read-only
 * from the consumer's perspective" choice
 * [com.lifeos.app.features.home.domain.model.PriorityTask]'s Home
 * integration already established, not a new pattern invented for Travel).
 *
 * Deliberately not a reuse of [com.lifeos.app.features.planner.presentation.sections.TaskCard]
 * (Planner's own presentation-layer composable) — Travel depends on
 * Planner's *domain* [Task] directly (the same cross-feature domain reuse
 * [com.lifeos.app.features.home.domain.model.PriorityTask.priority] already
 * established for Home), but never imports another feature's presentation
 * code, per this project's feature-first boundary. This row is built from
 * plain Design System primitives instead, mirroring the exact row shape
 * Home's own priority row already uses.
 *
 * Travel owns none of this data — [tasks] is fetched and filtered by
 * [com.lifeos.app.features.travel.presentation.TravelDetailViewModel] via
 * Planner's existing `GetPlannerDashboardUseCase`, never stored or
 * duplicated here.
 */
@Composable
fun TravelTasksSection(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.TASKS_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))

        if (tasks.isEmpty()) {
            EmptyState(
                title = TravelStrings.TASKS_EMPTY_TITLE,
                description = TravelStrings.TASKS_EMPTY_DESCRIPTION,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                tasks.forEach { task ->
                    key(task.id) {
                        TravelTaskRow(task = task, onClick = { onTaskClick(task.id) })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppOutlinedButton(text = TravelStrings.TASKS_ADD_ACTION, onClick = onAddTaskClick)
    }
}

@Composable
private fun TravelTaskRow(task: Task, onClick: () -> Unit) {
    val isCompleted = task.status == TaskStatus.DONE

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isCompleted) LifeOSTealBase else MaterialTheme.colorScheme.outline,
        )
        Spacer(modifier = Modifier.width(LifeOSSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = RelativeDateFormatter.format(task.dueDate.date, task.dueDate.time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun TravelTasksSectionPreview() {
    LifeOSTheme {
        TravelTasksSection(
            tasks = listOf(
                Task(
                    id = "task-cappadocia-trip",
                    title = "Kapadokya Seyahati",
                    description = "Balon turu ve mağara otel konaklaması",
                    dueDate = TaskDueDate(date = LocalDate(2024, 11, 12)),
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.TRAVEL,
                    status = TaskStatus.TODO,
                    source = TaskSource.TRAVEL,
                    tags = listOf("seyahat"),
                    hasReminder = true,
                    createdAt = LocalDate(2024, 10, 1),
                ),
            ),
            onTaskClick = {},
            onAddTaskClick = {},
        )
    }
}

@Preview
@Composable
private fun TravelTasksSectionEmptyPreview() {
    LifeOSTheme {
        TravelTasksSection(tasks = emptyList(), onTaskClick = {}, onAddTaskClick = {})
    }
}
