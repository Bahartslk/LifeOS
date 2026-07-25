package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.lifeos.app.core.designsystem.DesignSystemStrings
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase

/**
 * Password input matching login.png: a lock leading icon, an eye/eye-off
 * trailing toggle, and an optional inline "Forgot Password?" link beside the
 * label (via [onForgotPasswordClick] — pass `null` to omit it, e.g. on a
 * "confirm password" field where it doesn't apply).
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    forgotPasswordLabel: String? = null,
    onForgotPasswordClick: (() -> Unit)? = null,
) {
    var isVisible by remember { mutableStateOf(false) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Filled.Lock,
        trailingContent = {
            IconButton(onClick = { isVisible = !isVisible }) {
                AppIcon(
                    imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (isVisible) {
                        DesignSystemStrings.PASSWORD_HIDE
                    } else {
                        DesignSystemStrings.PASSWORD_SHOW
                    },
                )
            }
        },
        topEndContent = if (forgotPasswordLabel != null && onForgotPasswordClick != null) {
            {
                Text(
                    text = forgotPasswordLabel,
                    color = LifeOSVioletBase,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable(onClick = onForgotPasswordClick),
                )
            }
        } else {
            null
        },
        isError = isError,
        supportingText = supportingText,
        singleLine = true,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
    )
}
