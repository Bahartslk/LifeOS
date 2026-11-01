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
import com.lifeos.app.features.planner.domain.model.TaskType
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** "Görev / Toplantı". A meeting needs a time and gets a place; see [com.lifeos.app.features.planner.presentation.CreateTaskViewModel]. */
@Composable
fun TaskTypeSection(
    selectedType: TaskType,
    onTypeSelected: (TaskType) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.TASK_TYPE_TITLE, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        OptionChipRow(
            options = TaskType.entries,
            selectedOption = selectedType,
            labelFor = { it.toLabel() },
            onOptionSelected = onTypeSelected,
        )
    }
}

internal fun TaskType.toLabel(): String = when (this) {
    TaskType.TASK -> PlannerStrings.TASK_TYPE_TASK
    TaskType.MEETING -> PlannerStrings.TASK_TYPE_MEETING
}

@Preview
@Composable
private fun TaskTypeSectionPreview() {
    LifeOSTheme {
        TaskTypeSection(selectedType = TaskType.MEETING, onTypeSelected = {})
    }
}
