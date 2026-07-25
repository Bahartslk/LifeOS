package com.lifeos.app.features.ai.presentation.sections

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.ai.domain.model.AiTaskProgress
import com.lifeos.app.features.ai.presentation.AiAssistantStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Progress Summary" (this integration's requirement) — real completion
 * counts straight from [com.lifeos.app.features.planner.domain.model.PlannerOverview],
 * never an AI-invented percentage, matching the same "real numbers, no
 * fake percentages" rule the Home integration already applied to its own
 * Overview tile.
 */
@Composable
fun AiProgressSummarySection(
    progress: AiTaskProgress,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = AiAssistantStrings.PROGRESS_TITLE, style = LifeOSTextStyles.overline)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Row {
            Text(text = "${progress.completionPercent}%", style = LifeOSTextStyles.statDisplay)
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(
            text = AiAssistantStrings.progressCountLabel(progress.completedCount, progress.totalCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        LinearProgressIndicator(
            progress = { progress.completionRatio() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun AiTaskProgress.completionRatio(): Float =
    if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

@Preview
@Composable
private fun AiProgressSummarySectionPreview() {
    LifeOSTheme {
        AiProgressSummarySection(
            progress = AiTaskProgress(completedCount = 8, totalCount = 12, completionPercent = 92),
        )
    }
}
