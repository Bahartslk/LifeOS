package com.lifeos.app.features.auth.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.auth.presentation.AuthStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The email [AppTextField] wiring repeated identically on Login, Register,
 * and Forgot Password (same label/placeholder/leading icon/error shape) —
 * extracted here once three screens had copy-pasted it, per this task's
 * "avoid duplicated UI" rule.
 */
@Composable
fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = AuthStrings.LOGIN_EMAIL_LABEL,
        placeholder = AuthStrings.LOGIN_EMAIL_PLACEHOLDER,
        leadingIcon = Icons.Filled.Email,
        isError = error != null,
        supportingText = error,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun AuthEmailFieldPreview() {
    LifeOSTheme {
        AuthEmailField(value = "demo@lifeos.app", onValueChange = {}, error = null)
    }
}

@Preview
@Composable
private fun AuthEmailFieldErrorPreview() {
    LifeOSTheme {
        AuthEmailField(value = "invalid-email", onValueChange = {}, error = "Geçerli bir e-posta adresi girin.")
    }
}
