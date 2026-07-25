package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The task's [TaskCategory] (this task's requirement). Named
 * `TaskCategorySection` rather than `CategorySection` to avoid colliding
 * with [CategorySection] (the Planner Dashboard's tappable Quick Categories
 * filter) — same package, genuinely different responsibility: this one is
 * read-only display, not a selector. Reuses [TaskCategory.toIcon] and
 * [TaskCategory.toLabel] (both `internal`, defined in `TaskCard.kt` and
 * `CategorySection.kt` respectively) rather than duplicating the two
 * `when` mappings a second time.
 */
@Composable
fun TaskCategorySection(
    category: TaskCategory,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.TASK_CATEGORY_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(LifeOSSize.avatarSmall)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = category.toIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    size = LifeOSSize.iconSmall,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Text(text = category.toLabel(), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview
@Composable
private fun TaskCategorySectionPreview() {
    LifeOSTheme {
        TaskCategorySection(category = TaskCategory.WORK)
    }
}
