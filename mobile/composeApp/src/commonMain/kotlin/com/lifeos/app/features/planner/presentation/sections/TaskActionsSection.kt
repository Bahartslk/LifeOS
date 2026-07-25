package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Completion Button" + "Delete Button" (this task's requirement). The
 * confirmation dialog for delete is shown by
 * [com.lifeos.app.features.planner.presentation.TaskDetailScreen] itself
 * (an overlay, not a section) via
 * [com.lifeos.app.core.designsystem.components.ConfirmationDialog] — the
 * same "dialogs live at the Screen level" precedent every other feature
 * follows.
 */
@Composable
fun TaskActionsSection(
    isCompleted: Boolean,
    onCompleteToggleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
        AppPrimaryButton(
            text = if (isCompleted) PlannerStrings.ACTION_MARK_INCOMPLETE else PlannerStrings.ACTION_MARK_COMPLETE,
            onClick = onCompleteToggleClick,
        )
        AppOutlinedButton(
            text = PlannerStrings.ACTION_DELETE,
            onClick = onDeleteClick,
            leadingIcon = Icons.Filled.Delete,
        )
    }
}

@Preview
@Composable
private fun TaskActionsSectionIncompletePreview() {
    LifeOSTheme {
        TaskActionsSection(isCompleted = false, onCompleteToggleClick = {}, onDeleteClick = {})
    }
}

@Preview
@Composable
private fun TaskActionsSectionCompletedPreview() {
    LifeOSTheme {
        TaskActionsSection(isCompleted = true, onCompleteToggleClick = {}, onDeleteClick = {})
    }
}
