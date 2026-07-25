package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AiBadge
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.PlannerOverview
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Today's Overview" + "Today's Productivity" (planner.png: the four-number
 * stat card with the "AI Verimlilik" badge) — both requirements map onto
 * this one Stitch card, since Stitch itself never separates them into two
 * surfaces. [TaskProgressIndicator] is appended beneath the four numbers as
 * this task's explicit "Today's Productivity" visualization, mirroring
 * [com.lifeos.app.features.home.presentation.sections.OverviewSection]'s
 * own completed-tasks progress bar.
 */
@Composable
fun TodayOverviewCard(
    overview: PlannerOverview,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            AiBadge(label = PlannerStrings.AI_PRODUCTIVITY_BADGE, icon = Icons.Filled.AutoAwesome)
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OverviewStatColumn(label = PlannerStrings.STAT_TOTAL_TASKS, value = overview.totalTaskCount.toString())
            OverviewStatColumn(
                label = PlannerStrings.STAT_UPCOMING_EVENTS,
                value = overview.upcomingEventCount.toString(),
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OverviewStatColumn(label = PlannerStrings.STAT_COMPLETED, value = overview.completedTaskCount.toString())
            OverviewStatColumn(
                label = PlannerStrings.STAT_PRODUCTIVITY,
                value = "${overview.productivityPercent}%",
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        TaskProgressIndicator(completedCount = overview.completedTaskCount, totalCount = overview.totalTaskCount)
    }
}

@Composable
private fun OverviewStatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = LifeOSTextStyles.overline, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        Text(text = value, style = LifeOSTextStyles.statDisplay)
    }
}

@Preview
@Composable
private fun TodayOverviewCardPreview() {
    LifeOSTheme {
        TodayOverviewCard(
            overview = PlannerOverview(
                totalTaskCount = 12,
                upcomingEventCount = 3,
                completedTaskCount = 8,
                productivityPercent = 92,
            ),
        )
    }
}
