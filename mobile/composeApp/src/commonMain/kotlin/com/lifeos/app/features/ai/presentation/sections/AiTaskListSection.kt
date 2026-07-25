package com.lifeos.app.features.ai.presentation.sections

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
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * One AI-context category (Today's/Upcoming/High-Priority/Overdue/Travel
 * Tasks — this integration's requirement) — reused five times by
 * `AiAssistantScreen.kt` with different [title]/[tasks]/[emptyMessage]
 * rather than five near-duplicate composables. A small count [StatusChip]
 * next to the title stands in for "how many the assistant is looking at,"
 * matching how a real AI response would likely open ("3 göreviniz var...").
 *
 * Read-only (tap navigates to Task Detail, where completion is actually
 * toggled) — the same choice Home's and Travel's Planner integrations
 * already made for their own task rows, not a new pattern invented here.
 * A muted inline line, not a full [com.lifeos.app.core.designsystem.components.EmptyState],
 * represents "no tasks in this category" — proportionate for a compact
 * sub-section repeated five times on one screen, where a full empty-state
 * illustration five times over would be visual noise.
 */
@Composable
fun AiTaskListSection(
    title: String,
    tasks: List<Task>,
    emptyMessage: String,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            StatusChip(label = "${tasks.size}")
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))

        if (tasks.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                tasks.forEach { task ->
                    key(task.id) {
                        AiTaskRow(task = task, onClick = { onTaskClick(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AiTaskRow(task: Task, onClick: () -> Unit) {
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
private fun AiTaskListSectionPreview() {
    LifeOSTheme {
        AiTaskListSection(
            title = "Bugünün Görevleri",
            tasks = listOf(
                Task(
                    id = "task-project-work",
                    title = "Proje Çalışması",
                    description = null,
                    dueDate = TaskDueDate(date = LocalDate(2024, 10, 24)),
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.WORK,
                    status = TaskStatus.TODO,
                    source = TaskSource.PLANNER,
                    tags = emptyList(),
                    hasReminder = true,
                    createdAt = LocalDate(2024, 10, 18),
                ),
            ),
            emptyMessage = "Bugün için göreviniz yok.",
            onTaskClick = {},
        )
    }
}

@Preview
@Composable
private fun AiTaskListSectionEmptyPreview() {
    LifeOSTheme {
        AiTaskListSection(
            title = "Gecikmiş Görevler",
            tasks = emptyList(),
            emptyMessage = "Gecikmiş göreviniz yok. Harika gidiyorsunuz!",
            onTaskClick = {},
        )
    }
}
