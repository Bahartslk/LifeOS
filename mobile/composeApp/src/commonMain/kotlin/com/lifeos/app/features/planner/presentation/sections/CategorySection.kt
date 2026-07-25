package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.OptionChipRow
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Quick Categories" (this task's requirement — not literally pictured in
 * planner.png; see this feature's "Deviations from Stitch" note). Reuses
 * [OptionChipRow] — the same single-choice chip picker "Create Travel
 * (AI)" already uses — with `null` standing in for "Tümü" (all
 * categories), filtering [TodayTasksSection]/[UpcomingTasksSection]'s lists
 * by [Task.category][com.lifeos.app.features.planner.domain.model.Task.category].
 */
@Composable
fun CategorySection(
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = PlannerStrings.CATEGORIES_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        OptionChipRow(
            options = listOf<TaskCategory?>(null) + TaskCategory.entries,
            selectedOption = selectedCategory,
            labelFor = { category -> category?.toLabel() ?: PlannerStrings.CATEGORY_ALL },
            onOptionSelected = onCategorySelected,
        )
    }
}

/** `internal` (not `private`): `TaskCategorySection.kt` needs this exact same mapping. */
internal fun TaskCategory.toLabel(): String = when (this) {
    TaskCategory.WORK -> PlannerStrings.CATEGORY_WORK
    TaskCategory.PERSONAL -> PlannerStrings.CATEGORY_PERSONAL
    TaskCategory.HEALTH -> PlannerStrings.CATEGORY_HEALTH
    TaskCategory.TRAVEL -> PlannerStrings.CATEGORY_TRAVEL
    TaskCategory.FINANCE -> PlannerStrings.CATEGORY_FINANCE
}

@Preview
@Composable
private fun CategorySectionPreview() {
    LifeOSTheme {
        CategorySection(selectedCategory = null, onCategorySelected = {})
    }
}

@Preview
@Composable
private fun CategorySectionSelectedPreview() {
    LifeOSTheme {
        CategorySection(selectedCategory = TaskCategory.WORK, onCategorySelected = {})
    }
}
