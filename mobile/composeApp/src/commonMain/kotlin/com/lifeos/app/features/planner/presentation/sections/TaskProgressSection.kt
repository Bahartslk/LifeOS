package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.Subtask
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Progress" (this task's requirement): the task's subtask completion
 * ratio. Named `TaskProgressSection` rather than `ProgressSection` to avoid
 * colliding with [com.lifeos.app.features.planner.presentation.sections.ProgressSection]
 * (the Planner Dashboard's AI-insight card) — same package, unrelated
 * concept. Reuses [TaskProgressIndicator] (built generically enough during
 * the Planner Dashboard sprint to need no changes here) rather than a new
 * progress-bar composable.
 *
 * Renders nothing for a task with no subtasks — "0 / 0" would be a
 * meaningless progress bar, not a genuine empty state worth its own card.
 */
@Composable
fun TaskProgressSection(
    subtasks: List<Subtask>,
    modifier: Modifier = Modifier,
) {
    if (subtasks.isEmpty()) return

    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.TASK_PROGRESS_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        TaskProgressIndicator(
            completedCount = subtasks.count { it.isCompleted },
            totalCount = subtasks.size,
        )
    }
}

@Preview
@Composable
private fun TaskProgressSectionPreview() {
    LifeOSTheme {
        TaskProgressSection(
            subtasks = listOf(
                Subtask(id = "subtask-1", title = "Veri analizini tamamla", isCompleted = true),
                Subtask(id = "subtask-2", title = "Sunum taslağını hazırla", isCompleted = true),
                Subtask(id = "subtask-3", title = "Yöneticiyle gözden geçir", isCompleted = false),
                Subtask(id = "subtask-4", title = "Son düzeltmeleri yap", isCompleted = false),
            ),
        )
    }
}
