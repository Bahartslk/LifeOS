package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.date.AppDateFormatter
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppDialog
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Saat": tap to pick a time with Material 3's 24-hour time picker. The
 * picked time wins over a time typed into the due-date field. [errorMessage]
 * shows when a meeting or a reminder has no time.
 */
@Composable
fun TaskTimeSection(
    time: LocalTime?,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val valueLabel = time?.let(AppDateFormatter::toTimeLabel)
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = "${PlannerStrings.TIME_FIELD_CONTENT_DESCRIPTION}: ${valueLabel ?: PlannerStrings.TIME_PLACEHOLDER}"
            },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = PlannerStrings.TIME_LABEL, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = valueLabel ?: PlannerStrings.TIME_PLACEHOLDER,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (valueLabel != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        Text(
            text = errorMessage ?: PlannerStrings.TIME_OPTIONAL_HINT,
            style = MaterialTheme.typography.bodySmall,
            color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

/** The time picker dialog; [onClear] is offered only when a time is already set. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTimePickerDialog(
    initialTime: LocalTime?,
    onConfirm: (LocalTime) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime?.hour ?: DEFAULT_HOUR,
        initialMinute = initialTime?.minute ?: 0,
        is24Hour = true,
    )
    AppDialog(
        onDismissRequest = onDismiss,
        title = PlannerStrings.TIME_PICKER_TITLE,
        content = { TimePicker(state = state) },
        buttons = {
            AppPrimaryButton(
                text = PlannerStrings.TIME_PICKER_CONFIRM,
                onClick = { onConfirm(LocalTime(state.hour, state.minute)) },
            )
            if (initialTime != null) {
                AppOutlinedButton(text = PlannerStrings.TIME_CLEAR, onClick = onClear)
            }
            AppOutlinedButton(text = PlannerStrings.TIME_PICKER_DISMISS, onClick = onDismiss)
        },
    )
}

private const val DEFAULT_HOUR = 9

@Preview
@Composable
private fun TaskTimeSectionPreview() {
    LifeOSTheme {
        TaskTimeSection(time = LocalTime(10, 0), errorMessage = null, onClick = {})
    }
}

@Preview
@Composable
private fun TaskTimeSectionErrorPreview() {
    LifeOSTheme {
        TaskTimeSection(time = null, errorMessage = PlannerStrings.MEETING_TIME_REQUIRED_ERROR, onClick = {})
    }
}
