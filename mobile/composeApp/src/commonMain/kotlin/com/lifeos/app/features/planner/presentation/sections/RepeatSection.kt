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
import com.lifeos.app.features.planner.presentation.PlannerStrings
import com.lifeos.app.features.planner.presentation.RepeatOption
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Repeat Option" (Create Task's requirement — explicitly UI only). Reuses
 * [OptionChipRow], the same single-choice picker every other Create Task
 * field uses. [RepeatOption] deliberately lives in `CreateTaskContract.kt`
 * (presentation layer), not `domain/model` — it is never sent to
 * [com.lifeos.app.features.planner.domain.model.CreateTaskRequest] or
 * stored anywhere, so it has no business being a domain concept.
 */
@Composable
fun RepeatSection(
    selectedOption: RepeatOption,
    onOptionSelected: (RepeatOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.REPEAT_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        OptionChipRow(
            options = RepeatOption.entries,
            selectedOption = selectedOption,
            labelFor = { option -> option.toLabel() },
            onOptionSelected = onOptionSelected,
        )
    }
}

private fun RepeatOption.toLabel(): String = when (this) {
    RepeatOption.NONE -> PlannerStrings.REPEAT_NONE_LABEL
    RepeatOption.DAILY -> PlannerStrings.REPEAT_DAILY_LABEL
    RepeatOption.WEEKLY -> PlannerStrings.REPEAT_WEEKLY_LABEL
    RepeatOption.MONTHLY -> PlannerStrings.REPEAT_MONTHLY_LABEL
}

@Preview
@Composable
private fun RepeatSectionPreview() {
    LifeOSTheme {
        RepeatSection(selectedOption = RepeatOption.NONE, onOptionSelected = {})
    }
}
