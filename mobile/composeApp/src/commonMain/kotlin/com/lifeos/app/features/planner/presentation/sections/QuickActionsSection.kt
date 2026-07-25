package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.QuickActionTile
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** The fixed set of shortcuts in Planner's Quick Actions (this task's requirement) — not user data. */
enum class PlannerQuickAction { NEW_TASK, VIEW_CALENDAR, REMINDERS, VIEW_REPORT }

/** The four Planner shortcut tiles (this task's requirement) — a 2x2 grid of [QuickActionTile]s. */
@Composable
fun QuickActionsSection(
    onActionClick: (PlannerQuickAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = PlannerStrings.QUICK_ACTIONS_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
        ) {
            QuickActionTile(
                label = PlannerStrings.QUICK_ACTION_NEW_TASK,
                icon = Icons.Filled.AddTask,
                onClick = { onActionClick(PlannerQuickAction.NEW_TASK) },
                modifier = Modifier.weight(1f),
            )
            QuickActionTile(
                label = PlannerStrings.QUICK_ACTION_VIEW_CALENDAR,
                icon = Icons.Filled.CalendarMonth,
                onClick = { onActionClick(PlannerQuickAction.VIEW_CALENDAR) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
        ) {
            QuickActionTile(
                label = PlannerStrings.QUICK_ACTION_REMINDERS,
                icon = Icons.Filled.NotificationsActive,
                onClick = { onActionClick(PlannerQuickAction.REMINDERS) },
                modifier = Modifier.weight(1f),
            )
            QuickActionTile(
                label = PlannerStrings.QUICK_ACTION_VIEW_REPORT,
                icon = Icons.Filled.BarChart,
                onClick = { onActionClick(PlannerQuickAction.VIEW_REPORT) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun QuickActionsSectionPreview() {
    LifeOSTheme {
        QuickActionsSection(onActionClick = {})
    }
}
