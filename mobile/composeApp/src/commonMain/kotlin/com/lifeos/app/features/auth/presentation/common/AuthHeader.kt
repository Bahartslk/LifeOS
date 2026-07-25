package com.lifeos.app.features.auth.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase
import com.lifeos.app.features.auth.presentation.AuthStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The "LifeOS" wordmark shown top-left on Login/Register (login.png).
 * Feature-local rather than a Design System component: it is specific
 * Authentication-screen content (the brand wordmark plus this feature's
 * exact placement rules), not a generic reusable primitive other features
 * would compose with.
 */
@Composable
fun AuthWordmark(modifier: Modifier = Modifier) {
    Text(
        text = AuthStrings.APP_NAME,
        style = MaterialTheme.typography.headlineSmall,
        color = LifeOSVioletBase,
        modifier = modifier,
    )
}

/** The title + subtitle pair repeated at the top of Login, Register, and Forgot Password. */
@Composable
fun AuthScreenTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun AuthWordmarkPreview() {
    LifeOSTheme {
        AuthWordmark()
    }
}

@Preview
@Composable
private fun AuthScreenTitlePreview() {
    LifeOSTheme {
        AuthScreenTitle(title = AuthStrings.LOGIN_TITLE, subtitle = AuthStrings.LOGIN_SUBTITLE)
    }
}
