package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Session" (this sprint's requirement: Logout). Tapping shows a real
 * [com.lifeos.app.core.designsystem.components.ConfirmationDialog] (owned
 * by `ProfileScreen.kt`, the same "dialogs live at the Screen level"
 * precedent every other feature follows) — confirming it never clears the
 * real session or navigates away, per this sprint's explicit "Do NOT
 * implement real logout" scope.
 */
@Composable
fun SessionSection(
    isLoggingOut: Boolean,
    onLogoutClick: () -> Unit,
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
    }
}

@Preview
@Composable
private fun SessionSectionPreview() {
    LifeOSTheme {
        SessionSection(isLoggingOut = false, onLogoutClick = {})
    }
}
