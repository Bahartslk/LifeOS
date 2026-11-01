package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.OptionChipRow
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import com.lifeos.app.features.planner.presentation.REMINDER_OPTIONS
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Hatırlatıcı": how long before the task's time the push reminder comes
 * (Yok / Zamanında / 5 / 10 / 15 / 30 dk / 1 saat). Saved as the task's
 * `reminderMinutesBefore`; a reminder needs a time, which the form checks.
 */
@Composable
fun ReminderSection(
    reminderMinutesBefore: Int?,
    onReminderSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Text(text = PlannerStrings.REMINDER_LABEL, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = PlannerStrings.REMINDER_SUBTITLE,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        OptionChipRow(
            options = REMINDER_OPTIONS,
            selectedOption = reminderMinutesBefore,
            labelFor = { minutes -> minutes?.let(PlannerStrings::reminderOptionLabel) ?: PlannerStrings.REMINDER_NONE },
            onOptionSelected = onReminderSelected,
        )
    }
}

@Preview
@Composable
private fun ReminderSectionPreview() {
    LifeOSTheme {
        ReminderSection(reminderMinutesBefore = 10, onReminderSelected = {})
    }
}
