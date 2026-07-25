package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.InfoRow
import com.lifeos.app.core.designsystem.components.StatusChip
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
 * "Task Information" (this task's requirement): Description, Due Date,
 * Reminder, Status, and Tags — the general-purpose facts about a task that
 * don't warrant their own dedicated card the way Priority and Category do
 * (both already have a rich visual treatment elsewhere in this feature).
 * Reuses [InfoRow] for every "label above value" fact, the same component
 * Travel Detail's Flight/Hotel sections use. [Task.estimatedDurationLabel]
 * only renders when set (Create Task's requirement) — every task created
 * before that field existed simply has nothing to show here.
 */
@Composable
fun TaskInfoCard(
    task: Task,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.INFO_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        InfoRow(
            label = PlannerStrings.DESCRIPTION_LABEL,
            value = task.description ?: PlannerStrings.DESCRIPTION_EMPTY,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg)) {
            InfoRow(
                label = PlannerStrings.DUE_DATE_LABEL,
                value = task.dueDateDisplayLabel(),
                modifier = Modifier.weight(1f),
            )
            InfoRow(
                label = PlannerStrings.REMINDER_LABEL,
                value = if (task.hasReminder) PlannerStrings.REMINDER_ON else PlannerStrings.REMINDER_OFF,
                modifier = Modifier.weight(1f),
            )
        }
        if (task.estimatedDurationLabel != null) {
            Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
            InfoRow(label = PlannerStrings.ESTIMATED_DURATION_LABEL, value = task.estimatedDurationLabel)
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        InfoRow(label = PlannerStrings.STATUS_LABEL, value = task.status.toLabel())
        if (task.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
            TagsRow(tags = task.tags)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsRow(tags: List<String>) {
    Column {
        Text(
            text = PlannerStrings.TAGS_LABEL,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
            tags.forEach { tag -> StatusChip(label = tag) }
        }
    }
}

private fun TaskStatus.toLabel(): String = when (this) {
    TaskStatus.TODO -> PlannerStrings.TASK_STATUS_TODO_LABEL
    TaskStatus.IN_PROGRESS -> PlannerStrings.TASK_STATUS_IN_PROGRESS_LABEL
    TaskStatus.DONE -> PlannerStrings.STATUS_COMPLETED_LABEL
}

@Preview
@Composable
private fun TaskInfoCardPreview() {
    LifeOSTheme {
        TaskInfoCard(
            task = Task(
                id = "task-team-meeting",
                title = "Ekip Toplantısı",
                description = "Sprint planlama ve önceliklendirme görüşmesi",
                dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(10, 0)),
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.WORK,
                status = TaskStatus.IN_PROGRESS,
                source = TaskSource.PLANNER,
                tags = listOf("Ayşe", "Mehmet", "Zeynep", "Can"),
                hasReminder = true,
                createdAt = LocalDate(2024, 10, 21),
            ),
        )
    }
}

/** A task created via Create Task's flow, with [Task.estimatedDurationLabel] set. */
@Preview
@Composable
private fun TaskInfoCardWithEstimatedDurationPreview() {
    LifeOSTheme {
        TaskInfoCard(
            task = Task(
                id = "task-created-1",
                title = "Sunum hazırlığı",
                description = "Yönetim kurulu için slaytları tamamla",
                dueDate = TaskDueDate(date = LocalDate(2024, 10, 25), time = LocalTime(14, 0)),
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK,
                status = TaskStatus.TODO,
                source = TaskSource.PLANNER,
                tags = emptyList(),
                hasReminder = true,
                createdAt = LocalDate(2024, 10, 24),
                estimatedDurationLabel = "45 dakika",
            ),
        )
    }
}
