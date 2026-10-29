package com.lifeos.app.features.profile.presentation.dialogs

import androidx.compose.runtime.Composable
import com.lifeos.app.core.designsystem.components.AppDialog
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.EditNameDialogState
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Edits the display name shown on Profile and in Home's greeting. */
@Composable
fun EditNameDialog(
    state: EditNameDialogState,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = ProfileStrings.EDIT_NAME_TITLE,
        content = {
            AppTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = ProfileStrings.EDIT_NAME_LABEL,
                isError = state.errorMessage != null,
                supportingText = state.errorMessage,
                enabled = !state.isSaving,
            )
        },
        buttons = {
            AppPrimaryButton(
                text = if (state.isSaving) ProfileStrings.EDIT_NAME_SAVING else ProfileStrings.EDIT_NAME_SAVE,
                onClick = onSave,
                enabled = !state.isSaving,
                loading = state.isSaving,
            )
            AppOutlinedButton(
                text = ProfileStrings.EDIT_NAME_DISMISS,
                onClick = onDismiss,
                enabled = !state.isSaving,
            )
        },
    )
}

@Preview
@Composable
private fun EditNameDialogPreview() {
    LifeOSTheme {
        EditNameDialog(
            state = EditNameDialogState(name = "Bahar Toslak"),
            onNameChange = {},
            onSave = {},
            onDismiss = {},
        )
    }
}
