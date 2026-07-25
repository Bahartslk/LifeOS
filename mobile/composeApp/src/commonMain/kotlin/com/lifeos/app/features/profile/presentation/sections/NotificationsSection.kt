package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** "Notifications" (this sprint's requirement: Notification settings, Reminder settings) — both placeholder rows. */
@Composable
fun NotificationsSection(
    onNotificationSettingsClick: () -> Unit,
    onReminderSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = ProfileStrings.NOTIFICATIONS_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
                ProfileSettingsRow(
                    icon = Icons.Filled.Notifications,
                    label = ProfileStrings.NOTIFICATION_SETTINGS_ACTION,
                    onClick = onNotificationSettingsClick,
                )
                ProfileSettingsRow(
                    icon = Icons.Filled.Alarm,
                    label = ProfileStrings.REMINDER_SETTINGS_ACTION,
                    onClick = onReminderSettingsClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun NotificationsSectionPreview() {
    LifeOSTheme {
        NotificationsSection(onNotificationSettingsClick = {}, onReminderSettingsClick = {})
    }
}
