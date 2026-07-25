package com.lifeos.app.features.home.presentation.sections

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
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriority
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriorityContainer
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.home.domain.model.PriorityTask
import com.lifeos.app.features.home.presentation.HomeStrings
import com.lifeos.app.features.planner.domain.model.TaskPriority
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Today's Priority" (home.png): read-only from Home's perspective — tapping
 * a row navigates to Planner rather than toggling completion, per
 * [com.lifeos.app.features.home.domain.model.PriorityTask]'s KDoc.
 */
@Composable
fun TodaysPrioritiesSection(
    tasks: List<PriorityTask>,
    onManageAllClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = HomeStrings.PRIORITIES_TITLE,
            actionLabel = HomeStrings.PRIORITIES_MANAGE_ALL,
            onActionClick = onManageAllClick,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))

        if (tasks.isEmpty()) {
            EmptyState(
                title = HomeStrings.PRIORITIES_EMPTY_TITLE,
                description = HomeStrings.PRIORITIES_EMPTY_DESCRIPTION,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                tasks.forEach { task ->
                    key(task.id) {
                        PriorityTaskRow(task = task, onClick = { onTaskClick(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityTaskRow(task: PriorityTask, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.isCompleted) LifeOSTealBase else MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs)) {
                    StatusChip(label = task.category)
                    if (task.priority == TaskPriority.HIGH && !task.isCompleted) {
                        StatusChip(
                            label = HomeStrings.PRIORITY_HIGH_LABEL,
                            containerColor = LifeOSStatusHighPriorityContainer,
                            contentColor = LifeOSStatusHighPriority,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = task.dueLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TodaysPrioritiesSectionPreview() {
    LifeOSTheme {
        TodaysPrioritiesSection(
            tasks = listOf(
                PriorityTask(
                    id = "1",
                    title = "Q4 Bütçe Taslağını İncele",
                    category = "İŞ",
                    dueLabel = "14:00'te teslim",
                    isCompleted = false,
                    priority = TaskPriority.HIGH,
                ),
                PriorityTask(
                    id = "2",
                    title = "Sabah Meditasyonu",
                    category = "KİŞİSEL",
                    dueLabel = "Tamamlandı!",
                    isCompleted = true,
                    priority = TaskPriority.LOW,
                ),
            ),
            onManageAllClick = {},
            onTaskClick = {},
        )
    }
}

@Preview
@Composable
private fun TodaysPrioritiesSectionEmptyPreview() {
    LifeOSTheme {
        TodaysPrioritiesSection(tasks = emptyList(), onManageAllClick = {}, onTaskClick = {})
    }
}
