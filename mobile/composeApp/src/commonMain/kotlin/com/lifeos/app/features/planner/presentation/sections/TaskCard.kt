package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.date.RelativeDateFormatter
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriority
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriorityContainer
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase
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
 * One row in "Günlük Akış" (planner.png: time + title + a category icon
 * that becomes a checkmark once done, an in-progress task boxed with a
 * border). A completed task always shows a check regardless of
 * [Task.category] — Stitch's "Sabah Rutini" row does exactly this — so the
 * checkbox icon takes priority over the category icon, not the other way
 * around.
 *
 * The subtitle line is whichever is most relevant, in priority order:
 * completed status text, a high-priority badge, or a tag count (Stitch's
 * "+4" on "Ekip Toplantısı" — this app's [Task.tags] holds the four
 * attendee names, so the count is real data, not a magic string).
 */
@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleted = task.status == TaskStatus.DONE
    val isActive = task.status == TaskStatus.IN_PROGRESS
    val cardModifier = if (isActive) {
        modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), LifeOSShapes.large)
    } else {
        modifier
    }

    AppCard(modifier = cardModifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            IconButton(onClick = onToggle) {
                AppIcon(
                    imageVector = if (isCompleted) Icons.Filled.CheckCircle else task.category.toIcon(),
                    contentDescription = PlannerStrings.TASK_TOGGLE_CONTENT_DESCRIPTION,
                    tint = if (isCompleted) LifeOSTealBase else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.dueDateDisplayLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
                TaskCardSubtitle(task = task, isCompleted = isCompleted)
            }
        }
    }
}

@Composable
private fun TaskCardSubtitle(task: Task, isCompleted: Boolean) {
    when {
        isCompleted -> Text(
            text = PlannerStrings.STATUS_COMPLETED_LABEL,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        task.priority == TaskPriority.HIGH -> Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs)) {
            StatusChip(
                label = PlannerStrings.PRIORITY_HIGH_LABEL,
                containerColor = LifeOSStatusHighPriorityContainer,
                contentColor = LifeOSStatusHighPriority,
            )
        }
        task.tags.isNotEmpty() -> Text(
            text = PlannerStrings.tagCountLabel(task.tags.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** `internal` (not `private`): `UpcomingTasksSection.kt`'s `UpcomingTaskRow` needs this exact same mapping. */
internal fun TaskCategory.toIcon(): ImageVector = when (this) {
    TaskCategory.WORK -> Icons.Filled.Work
    TaskCategory.PERSONAL -> Icons.Filled.Person
    TaskCategory.HEALTH -> Icons.Filled.LocalHospital
    TaskCategory.TRAVEL -> Icons.Filled.FlightTakeoff
    TaskCategory.FINANCE -> Icons.Filled.AttachMoney
}

/**
 * `internal` (not `private`): every task list/card across Dashboard,
 * Calendar, and Task Detail needs this exact same due-date text — the
 * smart, relative [RelativeDateFormatter] label, plus a "– Şimdi" suffix
 * for whichever task is currently [TaskStatus.IN_PROGRESS] (Stitch's
 * "10:00 – Şimdi" on "Ekip Toplantısı"). That suffix is a status
 * decision, not a date-formatting one, so it's applied here rather than
 * inside [RelativeDateFormatter] itself, which has no [Task] knowledge.
 */
internal fun Task.dueDateDisplayLabel(): String {
    val base = RelativeDateFormatter.format(dueDate.date, dueDate.time)
    return if (status == TaskStatus.IN_PROGRESS) "$base – ${PlannerStrings.DUE_DATE_NOW_SUFFIX}" else base
}

@Preview
@Composable
private fun TaskCardCompletedPreview() {
    LifeOSTheme {
        TaskCard(
            task = Task(
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
            onToggle = {},
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun TaskCardActivePreview() {
    LifeOSTheme {
        TaskCard(
            task = Task(
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
            onToggle = {},
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun TaskCardHighPriorityPreview() {
    LifeOSTheme {
        TaskCard(
            task = Task(
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
            onToggle = {},
            onClick = {},
        )
    }
}
