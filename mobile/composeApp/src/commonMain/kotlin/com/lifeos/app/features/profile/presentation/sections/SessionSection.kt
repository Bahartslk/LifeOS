package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Session": Logout, and below it the quieter, error-colored "Hesabı Sil".
 * Both open a dialog owned by `ProfileScreen.kt` (the "dialogs live at the
 * Screen level" precedent every other feature follows).
 */
@Composable
fun SessionSection(
    isLoggingOut: Boolean,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = ProfileStrings.SESSION_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppOutlinedButton(
            text = if (isLoggingOut) ProfileStrings.LOGOUT_IN_PROGRESS else ProfileStrings.LOGOUT_ACTION,
            onClick = onLogoutClick,
            enabled = !isLoggingOut,
            leadingIcon = Icons.AutoMirrored.Filled.Logout,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        TextButton(
            onClick = onDeleteAccountClick,
            enabled = !isLoggingOut,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(LifeOSSize.buttonHeight),
        ) {
            Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(LifeOSSpacing.sm))
            Text(text = ProfileStrings.DELETE_ACCOUNT_ACTION)
        }
    }
}

@Preview
@Composable
private fun SessionSectionPreview() {
    LifeOSTheme {
        SessionSection(isLoggingOut = false, onLogoutClick = {}, onDeleteAccountClick = {})
    }
}
