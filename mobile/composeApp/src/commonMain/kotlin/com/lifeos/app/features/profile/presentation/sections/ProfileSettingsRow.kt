package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * One row in a Profile settings group (profile.png's "Hesap Ayarları"/
 * "Destek" lists: leading icon + label + optional trailing value +
 * chevron, the whole row tappable) — reused by every section on this
 * screen ([AccountSection], [PreferencesSection], [NotificationsSection],
 * [AboutSection]) rather than five near-duplicate row composables. Stays
 * feature-local (not promoted to the Design System) since only Profile
 * needs this exact shape today, per this project's "promote only once a
 * second feature needs the identical shape" rule.
 */
@Composable
fun ProfileSettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingValue: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(LifeOSSpacing.md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (trailingValue != null) {
            Text(
                text = trailingValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(LifeOSSpacing.xs))
        }
        AppIcon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun ProfileSettingsRowPreview() {
    LifeOSTheme {
        ProfileSettingsRow(
            icon = Icons.Filled.Language,
            label = "Dil",
            trailingValue = "Türkçe",
            onClick = {},
        )
    }
}
