package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Save Button" + "Cancel Button" (Create Task's requirement), mirroring
 * Task Detail's [TaskActionsSection] shape: a primary action on top, a
 * secondary/destructive-toned action below. [isSaving] disables both and
 * shows [AppPrimaryButton]'s built-in loading spinner — the same "Support
 * Loading" requirement [com.lifeos.app.features.travel.presentation.sections.ActionButtons]
 * already satisfies for Create Travel's own Save button.
 */
@Composable
fun SaveActionsSection(
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
        AppPrimaryButton(
            text = PlannerStrings.SAVE_ACTION,
            onClick = onSaveClick,
            enabled = !isSaving,
            loading = isSaving,
        )
        AppOutlinedButton(
            text = PlannerStrings.CANCEL_ACTION,
            onClick = onCancelClick,
            enabled = !isSaving,
        )
    }
}

@Preview
@Composable
private fun SaveActionsSectionPreview() {
    LifeOSTheme {
        SaveActionsSection(isSaving = false, onSaveClick = {}, onCancelClick = {})
    }
}

@Preview
@Composable
private fun SaveActionsSectionSavingPreview() {
    LifeOSTheme {
        SaveActionsSection(isSaving = true, onSaveClick = {}, onCancelClick = {})
    }
}
