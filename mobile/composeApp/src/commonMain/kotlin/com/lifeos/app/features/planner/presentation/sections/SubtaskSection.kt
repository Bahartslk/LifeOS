package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.Subtask
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Subtasks" (this task's requirement): a checklist inside the task,
 * following the same interactive-checklist pattern
 * [com.lifeos.app.features.travel.presentation.sections.PackingChecklistSection]
 * already established (a [Checkbox] row per item, toggled by tapping
 * anywhere on the row).
 */
@Composable
fun SubtaskSection(
    subtasks: List<Subtask>,
    onSubtaskToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.SUBTASKS_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        if (subtasks.isEmpty()) {
            Text(
                text = PlannerStrings.SUBTASKS_EMPTY,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs)) {
                subtasks.forEach { subtask ->
                    key(subtask.id) {
                        SubtaskRow(subtask = subtask, onToggle = { onSubtaskToggled(subtask.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtaskRow(subtask: Subtask, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = subtask.isCompleted,
            onCheckedChange = { onToggle() },
            modifier = Modifier.semantics { contentDescription = PlannerStrings.SUBTASK_TOGGLE_CONTENT_DESCRIPTION },
        )
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
            color = if (subtask.isCompleted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun SubtaskSectionPreview() {
    LifeOSTheme {
        SubtaskSection(
            subtasks = listOf(
                Subtask(id = "subtask-1", title = "Veri analizini tamamla", isCompleted = true),
                Subtask(id = "subtask-2", title = "Sunum taslağını hazırla", isCompleted = true),
                Subtask(id = "subtask-3", title = "Yöneticiyle gözden geçir", isCompleted = false),
                Subtask(id = "subtask-4", title = "Son düzeltmeleri yap", isCompleted = false),
            ),
            onSubtaskToggled = {},
        )
    }
}

@Preview
@Composable
private fun SubtaskSectionEmptyPreview() {
    LifeOSTheme {
        SubtaskSection(subtasks = emptyList(), onSubtaskToggled = {})
    }
}
