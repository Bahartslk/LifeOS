package com.lifeos.app.features.profile.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AppDialog
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.PasswordTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.DeleteAccountDialogState
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Asks for the password before the account is deleted for good. The
 * warning is in the error color so it can't be missed; errors (wrong
 * password, too many attempts, no connection) show under the field and are
 * announced to screen readers.
 */
@Composable
fun DeleteAccountDialog(
    state: DeleteAccountDialogState,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = ProfileStrings.DELETE_ACCOUNT_TITLE,
        content = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = ProfileStrings.DELETE_ACCOUNT_WARNING,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                text = ProfileStrings.DELETE_ACCOUNT_PASSWORD_PROMPT,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PasswordTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = ProfileStrings.DELETE_ACCOUNT_PASSWORD_LABEL,
                isError = state.errorMessage != null,
                supportingText = state.errorMessage,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        },
        buttons = {
            AppPrimaryButton(
                text = if (state.isDeleting) ProfileStrings.DELETE_ACCOUNT_IN_PROGRESS else ProfileStrings.DELETE_ACCOUNT_CONFIRM,
                onClick = onConfirm,
                enabled = !state.isDeleting,
                loading = state.isDeleting,
            )
            AppOutlinedButton(
                text = ProfileStrings.DELETE_ACCOUNT_DISMISS,
                onClick = onDismiss,
                enabled = !state.isDeleting,
            )
        },
    )
}

@Preview
@Composable
private fun DeleteAccountDialogPreview() {
    LifeOSTheme {
        DeleteAccountDialog(
            state = DeleteAccountDialogState(errorMessage = ProfileStrings.DELETE_ACCOUNT_WRONG_PASSWORD),
            onPasswordChange = {},
            onConfirm = {},
            onDismiss = {},
        )
    }
}
