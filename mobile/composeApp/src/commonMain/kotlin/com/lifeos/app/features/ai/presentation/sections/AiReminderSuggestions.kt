package com.lifeos.app.features.ai.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.date.RelativeDateFormatter
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.features.ai.domain.model.DailyReminder
import com.lifeos.app.features.ai.presentation.AiAssistantStrings

/**
 * "Hatırlatma önerileri": each AI suggestion with its reason, suggested date
 * and an "Ajandaya ekle" button. The button shows progress while saving and
 * turns into a disabled "Eklendi" once the task exists, so a suggestion can't
 * be added twice. Shows nothing when there are no suggestions.
 */
@Composable
fun AiReminderSuggestions(
    reminders: List<DailyReminder>,
    addingReminders: Set<DailyReminder>,
    addedReminders: Set<DailyReminder>,
    onAddClick: (DailyReminder) -> Unit,
) {
    if (reminders.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
        reminders.forEach { reminder ->
            ReminderSuggestionRow(
                reminder = reminder,
                isAdding = reminder in addingReminders,
                isAdded = reminder in addedReminders,
                onAddClick = { onAddClick(reminder) },
            )
        }
    }
}

@Composable
private fun ReminderSuggestionRow(
    reminder: DailyReminder,
    isAdding: Boolean,
    isAdded: Boolean,
    onAddClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs)) {
        Text(text = reminder.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        if (reminder.reason.isNotEmpty()) {
            Text(
                text = reminder.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        reminder.recommendedDate?.let { date ->
            Text(
                text = AiAssistantStrings.reminderDateLabel(RelativeDateFormatter.format(date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AddToAgendaButton(title = reminder.title, isAdding = isAdding, isAdded = isAdded, onClick = onAddClick)
    }
}

@Composable
private fun AddToAgendaButton(title: String, isAdding: Boolean, isAdded: Boolean, onClick: () -> Unit) {
    val description = if (isAdded) {
        AiAssistantStrings.reminderAddedDescription(title)
    } else {
        AiAssistantStrings.reminderAddDescription(title)
    }
    OutlinedButton(
        onClick = onClick,
        enabled = !isAdding && !isAdded,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                isAdding -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                isAdded -> Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            if (isAdding || isAdded) Spacer(Modifier.width(LifeOSSpacing.xs))
            Text(if (isAdded) AiAssistantStrings.REMINDER_ADDED_LABEL else AiAssistantStrings.REMINDER_ADD_ACTION)
        }
    }
}
