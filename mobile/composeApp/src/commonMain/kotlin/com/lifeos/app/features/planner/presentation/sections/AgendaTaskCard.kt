package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriority
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriorityContainer
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
 * One row in "Daily Agenda" (this feature's requirement) — deliberately not
 * a reuse of [TaskCard]: that composable mandates a completion-toggle
 * checkbox, but toggling completion is not one of Planner Calendar's listed
 * functions (only "Navigate to Task Detail" is), so a checkbox here would
 * be a control that visibly does nothing — a real accessibility/UX defect,
 * not a shortcut. This card is tap-to-detail only, reusing the same
 * icon-bubble shape [TaskCategorySection] already established.
 */
@Composable
fun AgendaTaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleted = task.status == TaskStatus.DONE

    AppCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                )
                Text(
                    text = task.dueDateDisplayLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!isCompleted && task.priority == TaskPriority.HIGH) {
                Spacer(modifier = Modifier.width(LifeOSSpacing.sm))
                StatusChip(
                    label = PlannerStrings.PRIORITY_HIGH_LABEL,
                    containerColor = LifeOSStatusHighPriorityContainer,
                    contentColor = LifeOSStatusHighPriority,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AgendaTaskCardPreview() {
    LifeOSTheme {
        AgendaTaskCard(
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
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun AgendaTaskCardCompletedPreview() {
    LifeOSTheme {
        AgendaTaskCard(
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
            onClick = {},
        )
    }
}
