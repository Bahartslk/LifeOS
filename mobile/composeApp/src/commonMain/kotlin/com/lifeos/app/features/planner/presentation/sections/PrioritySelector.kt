package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.OptionChipRow
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Priority Selector" (Create Task's requirement) — an interactive
 * single-choice picker, reusing [OptionChipRow] the same way "Create Travel
 * (AI)"'s Travel Style/Companions/Transportation/Accommodation Preference
 * sections do, rather than [PrioritySection]'s read-only 3-segment display
 * (Task Detail's variant, which has nothing to select from).
 */
@Composable
fun PrioritySelector(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.PRIORITY_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        OptionChipRow(
            options = TaskPriority.entries,
            selectedOption = selectedPriority,
            labelFor = { priority -> priority.toLabel() },
            onOptionSelected = onPrioritySelected,
        )
    }
}

private fun TaskPriority.toLabel(): String = when (this) {
    TaskPriority.LOW -> PlannerStrings.TASK_PRIORITY_LOW_LABEL
    TaskPriority.MEDIUM -> PlannerStrings.TASK_PRIORITY_MEDIUM_LABEL
    TaskPriority.HIGH -> PlannerStrings.TASK_PRIORITY_HIGH_LABEL
}

@Preview
@Composable
private fun PrioritySelectorPreview() {
    LifeOSTheme {
        PrioritySelector(selectedPriority = TaskPriority.MEDIUM, onPrioritySelected = {})
    }
}
