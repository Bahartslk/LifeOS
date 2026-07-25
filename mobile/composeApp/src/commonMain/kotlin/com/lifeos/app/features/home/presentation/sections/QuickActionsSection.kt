package com.lifeos.app.features.home.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.QuickActionTile
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.home.presentation.HomeQuickAction
import com.lifeos.app.features.home.presentation.HomeStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The four shortcut tiles (home.png: "New Task", "Create Trip", "Ask AI",
 * "Notes") — a 2x2 grid of [QuickActionTile]s.
 */
@Composable
fun QuickActionsSection(
    onActionClick: (HomeQuickAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
        ) {
            QuickActionTile(
                label = HomeStrings.QUICK_ACTION_NEW_TASK,
                icon = Icons.Filled.AddTask,
                onClick = { onActionClick(HomeQuickAction.NEW_TASK) },
                modifier = Modifier.weight(1f),
            )
            QuickActionTile(
                label = HomeStrings.QUICK_ACTION_CREATE_TRIP,
                icon = Icons.Filled.FlightTakeoff,
                onClick = { onActionClick(HomeQuickAction.CREATE_TRIP) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
        ) {
            QuickActionTile(
                label = HomeStrings.QUICK_ACTION_ASK_AI,
                icon = Icons.Filled.AutoAwesome,
                onClick = { onActionClick(HomeQuickAction.ASK_AI) },
                modifier = Modifier.weight(1f),
            )
            QuickActionTile(
                label = HomeStrings.QUICK_ACTION_CREATE_NOTE,
                icon = Icons.Filled.EditNote,
                onClick = { onActionClick(HomeQuickAction.CREATE_NOTE) },
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
