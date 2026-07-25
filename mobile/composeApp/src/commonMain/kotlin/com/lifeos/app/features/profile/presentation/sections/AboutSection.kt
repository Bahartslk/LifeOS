package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "About" (this sprint's requirement: App Version, Privacy Policy, Terms
 * of Service). [ProfileStrings.APP_VERSION_VALUE] is a genuine, real
 * constant (not a "coming soon" placeholder) — Privacy Policy and Terms of
 * Service, which have no content to show yet, are.
 */
@Composable
fun AboutSection(
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = ProfileStrings.ABOUT_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
                ProfileSettingsRow(
                    icon = Icons.Filled.PrivacyTip,
                    label = ProfileStrings.PRIVACY_POLICY_ACTION,
                    onClick = onPrivacyPolicyClick,
                )
                ProfileSettingsRow(
                    icon = Icons.Filled.Article,
                    label = ProfileStrings.TERMS_OF_SERVICE_ACTION,
                    onClick = onTermsOfServiceClick,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(LifeOSSpacing.md))
                    Text(
                        text = ProfileStrings.APP_VERSION_LABEL,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = ProfileStrings.APP_VERSION_VALUE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AboutSectionPreview() {
    LifeOSTheme {
        AboutSection(onPrivacyPolicyClick = {}, onTermsOfServiceClick = {})
    }
}
