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
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Category Selector" (Create Task's requirement) — an interactive,
 * always-one-selected picker, unlike the Dashboard's [CategorySection] (a
 * "Tümü"/all-categories filter with a nullable selection). Reuses
 * [OptionChipRow] and [toLabel] rather than a `null`-accepting variant or a
 * duplicated label mapping.
 */
@Composable
fun CategorySelector(
    selectedCategory: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.TASK_CATEGORY_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        OptionChipRow(
            options = TaskCategory.entries,
            selectedOption = selectedCategory,
            labelFor = { category -> category.toLabel() },
            onOptionSelected = onCategorySelected,
        )
    }
}

@Preview
@Composable
private fun CategorySelectorPreview() {
    LifeOSTheme {
        CategorySelector(selectedCategory = TaskCategory.WORK, onCategorySelected = {})
    }
}
