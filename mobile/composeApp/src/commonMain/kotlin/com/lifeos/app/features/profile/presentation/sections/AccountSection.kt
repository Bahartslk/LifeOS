package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Account" (this sprint's requirement: Edit Profile, Security, Change
 * Password) — Edit Profile itself lives on [ProfileHeader]'s own button,
 * not repeated here; this card covers the remaining two, both placeholder
 * actions ("Do NOT implement real editing").
 */
@Composable
fun AccountSection(
    onSecurityClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = ProfileStrings.ACCOUNT_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
                ProfileSettingsRow(
                    icon = Icons.Filled.Security,
                    label = ProfileStrings.SECURITY_ACTION,
                    onClick = onSecurityClick,
                )
                ProfileSettingsRow(
                    icon = Icons.Filled.Lock,
                    label = ProfileStrings.CHANGE_PASSWORD_ACTION,
                    onClick = onChangePasswordClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AccountSectionPreview() {
    LifeOSTheme {
        AccountSection(onSecurityClick = {}, onChangePasswordClick = {})
    }
}
