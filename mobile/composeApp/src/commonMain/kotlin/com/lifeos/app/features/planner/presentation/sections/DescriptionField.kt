package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** "Description" (Create Task's requirement) — optional, multi-line, matching [AppNotesField]'s `minLines` shape. */
@Composable
fun DescriptionField(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = PlannerStrings.DESCRIPTION_LABEL,
            placeholder = PlannerStrings.DESCRIPTION_FIELD_PLACEHOLDER,
            singleLine = false,
            minLines = DESCRIPTION_MIN_LINES,
        )
    }
}

private const val DESCRIPTION_MIN_LINES = 3

@Preview
@Composable
private fun DescriptionFieldPreview() {
    LifeOSTheme {
        DescriptionField(description = "", onDescriptionChanged = {})
    }
}
