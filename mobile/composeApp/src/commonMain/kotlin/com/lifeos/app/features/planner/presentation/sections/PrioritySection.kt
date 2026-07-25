package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriority
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriorityContainer
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Priority Visualization" (this task's requirement): a read-only 3-segment
 * scale highlighting the task's current [TaskPriority]. Editing priority is
 * out of scope (that belongs to the future Create/Edit Task screen), so
 * unlike [CategorySection]'s tappable filter chips, these segments are
 * purely informational. [TaskPriority.HIGH] reuses the same
 * red [LifeOSStatusHighPriority] tone [TaskCard]'s "! Önemli" badge already
 * established; [TaskPriority.LOW]/[TaskPriority.MEDIUM] use the generic
 * violet "selected" tone every other selectable chip in this app uses.
 */
@Composable
fun PrioritySection(
    priority: TaskPriority,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.PRIORITY_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
        ) {
            PrioritySegment(
                label = PlannerStrings.TASK_PRIORITY_LOW_LABEL,
                isSelected = priority == TaskPriority.LOW,
                isHigh = false,
                modifier = Modifier.weight(1f),
            )
            PrioritySegment(
                label = PlannerStrings.TASK_PRIORITY_MEDIUM_LABEL,
                isSelected = priority == TaskPriority.MEDIUM,
                isHigh = false,
                modifier = Modifier.weight(1f),
            )
            PrioritySegment(
                label = PlannerStrings.TASK_PRIORITY_HIGH_LABEL,
                isSelected = priority == TaskPriority.HIGH,
                isHigh = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PrioritySegment(
    label: String,
    isSelected: Boolean,
    isHigh: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        !isSelected -> MaterialTheme.colorScheme.surfaceVariant
        isHigh -> LifeOSStatusHighPriorityContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when {
        !isSelected -> MaterialTheme.colorScheme.onSurfaceVariant
        isHigh -> LifeOSStatusHighPriority
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .clip(LifeOSShapes.medium)
            .background(containerColor)
            .padding(vertical = LifeOSSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun PrioritySectionHighPreview() {
    LifeOSTheme {
        PrioritySection(priority = TaskPriority.HIGH)
    }
}

@Preview
@Composable
private fun PrioritySectionMediumPreview() {
    LifeOSTheme {
        PrioritySection(priority = TaskPriority.MEDIUM)
    }
}
