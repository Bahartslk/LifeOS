package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Actions" (this task's screen content: Düzenle / Yeniden Oluştur /
 * Seyahati Kaydet) — shown once a draft exists. [onSaveClicked] is the
 * single primary action per docs/06-design-system.md's "one primary CTA per
 * screen" rule; Edit/Regenerate are secondary, equal-weight choices.
 */
@Composable
fun ActionButtons(
    onEditClicked: () -> Unit,
    onRegenerateClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
        ) {
            AppOutlinedButton(
                text = TravelStrings.ACTION_EDIT,
                onClick = onEditClicked,
                modifier = Modifier.weight(1f),
            )
            AppOutlinedButton(
                text = TravelStrings.ACTION_REGENERATE,
                onClick = onRegenerateClicked,
                modifier = Modifier.weight(1f),
            )
        }
        AppPrimaryButton(
            text = TravelStrings.ACTION_SAVE_TRIP,
            onClick = onSaveClicked,
            loading = isSaving,
        )
    }
}

@Preview
@Composable
private fun ActionButtonsPreview() {
    LifeOSTheme {
        ActionButtons(onEditClicked = {}, onRegenerateClicked = {}, onSaveClicked = {})
    }
}
