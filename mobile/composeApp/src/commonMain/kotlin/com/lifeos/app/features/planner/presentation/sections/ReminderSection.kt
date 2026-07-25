package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Reminder" (Create Task's requirement) — a single settings-style toggle
 * row, reusing Material3's [Switch] directly rather than a new Design
 * System component, the same "reach for the Material3 primitive directly
 * for a simple control" precedent [SubtaskSection]'s [androidx.compose.material3.Checkbox]
 * already established.
 */
@Composable
fun ReminderSection(
    hasReminder: Boolean,
    onReminderToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = PlannerStrings.REMINDER_LABEL, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = PlannerStrings.REMINDER_TOGGLE_SUBTITLE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Switch(
                checked = hasReminder,
                onCheckedChange = onReminderToggled,
                modifier = Modifier.semantics {
                    contentDescription = PlannerStrings.REMINDER_TOGGLE_CONTENT_DESCRIPTION
                },
            )
        }
    }
}

@Preview
@Composable
private fun ReminderSectionOffPreview() {
    LifeOSTheme {
        ReminderSection(hasReminder = false, onReminderToggled = {})
    }
}

@Preview
@Composable
private fun ReminderSectionOnPreview() {
    LifeOSTheme {
        ReminderSection(hasReminder = true, onReminderToggled = {})
    }
}
