package com.lifeos.app.features.home.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.home.domain.model.OverviewStats
import com.lifeos.app.features.home.presentation.HomeStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The "Overview" section (home.png): completed-task and productivity stat
 * tiles, plus a weekly completion chart this task's requirements call for
 * beyond what the Stitch mockup itself shows (see this feature's
 * "Deviations from Stitch" note).
 */
@Composable
fun OverviewSection(
    stats: OverviewStats,
    onViewInsightsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = HomeStrings.OVERVIEW_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))

        Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
            TasksCompletedTile(stats = stats, modifier = Modifier.weight(1f))
            ProductivityTile(stats = stats, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppCard(modifier = Modifier.fillMaxWidth(), onClick = onViewInsightsClick) {
            Text(text = HomeStrings.OVERVIEW_VIEW_INSIGHTS, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(LifeOSSpacing.md))
            WeeklyProgressChart(ratios = stats.weeklyCompletionRatios)
        }
    }
}

@Composable
private fun TasksCompletedTile(stats: OverviewStats, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Text(text = HomeStrings.OVERVIEW_TASKS_COMPLETED_LABEL, style = LifeOSTextStyles.overline)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Row {
            Text(
                text = "${stats.completedTaskCount}",
                style = LifeOSTextStyles.statDisplay,
            )
            Text(
                text = " / ${stats.totalTaskCount}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        LinearProgressIndicator(
            progress = { stats.completionRatio() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProductivityTile(stats: OverviewStats, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Text(text = HomeStrings.OVERVIEW_PRODUCTIVITY_LABEL, style = LifeOSTextStyles.overline)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${stats.productivityPercent}%", style = LifeOSTextStyles.statDisplay)
            Spacer(modifier = Modifier.width(LifeOSSpacing.sm))
            AppIcon(
                imageVector = Icons.Filled.TrendingUp,
                contentDescription = null,
                tint = LifeOSTealBase,
            )
        }
        // No week-over-week comparison sentence when there's no real trend
        // data to back it — see OverviewStats.productivityDeltaPercent's
        // KDoc for why `null` here is an honest state, not a loading gap.
        stats.productivityDeltaPercent?.let { deltaPercent ->
            Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
            Text(
                text = HomeStrings.overviewProductivitySummary(deltaPercent),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun OverviewStats.completionRatio(): Float =
    if (totalTaskCount == 0) 0f else completedTaskCount.toFloat() / totalTaskCount.toFloat()

@Preview
@Composable
private fun OverviewSectionPreview() {
    LifeOSTheme {
        OverviewSection(
            stats = OverviewStats(
                completedTaskCount = 18,
                totalTaskCount = 24,
                productivityPercent = 92,
                productivityDeltaPercent = 12,
                weeklyCompletionRatios = listOf(0.6f, 0.8f, 0.5f, 0.9f, 0.7f, 0.85f, 0.75f),
            ),
            onViewInsightsClick = {},
        )
    }
}
