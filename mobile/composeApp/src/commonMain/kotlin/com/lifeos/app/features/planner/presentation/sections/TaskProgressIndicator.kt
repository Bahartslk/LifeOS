package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Today's completion ratio as a labeled bar — this task's "Today's
 * Productivity" requirement, visualized inside [TodayOverviewCard] the same
 * way [com.lifeos.app.features.home.presentation.sections.OverviewSection]'s
 * "Tasks Completed" tile shows a bar under its own count. A standalone
 * composable (not inlined into [TodayOverviewCard]) since it has its own
 * single responsibility and is independently previewable.
 */
@Composable
fun TaskProgressIndicator(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val ratio = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = PlannerStrings.PROGRESS_LABEL, style = MaterialTheme.typography.labelMedium)
            Text(
                text = PlannerStrings.progressCountLabel(completedCount, totalCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun TaskProgressIndicatorPreview() {
    LifeOSTheme {
        TaskProgressIndicator(completedCount = 8, totalCount = 12)
    }
}
