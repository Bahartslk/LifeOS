package com.lifeos.app.core.designsystem.components

import androidx.compose.runtime.Composable

/**
 * Confirmation dialog for destructive actions (delete trip, delete task),
 * per docs/06-design-system.md#components and docs/04-user-flow.md#edge-cases.
 * A thin convenience wrapper over [AppDialog] with the standard
 * confirm/cancel button arrangement.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = title,
        body = message,
        buttons = {
            AppPrimaryButton(text = confirmLabel, onClick = onConfirm)
            AppOutlinedButton(text = dismissLabel, onClick = onDismiss)
        },
    )
}
