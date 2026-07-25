package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Task Title" (Create Task's requirement) — the one required identity
 * field, so [errorMessage] surfaces inline via [AppTextField]'s own
 * `isError`/`supportingText`, the same validation-display pattern every
 * other form field in this codebase already uses (no separate error banner).
 */
@Composable
fun TaskTitleField(
    title: String,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = title,
            onValueChange = onTitleChanged,
            label = PlannerStrings.TITLE_FIELD_LABEL,
            placeholder = PlannerStrings.TITLE_FIELD_PLACEHOLDER,
            isError = errorMessage != null,
            supportingText = errorMessage,
        )
    }
}

@Preview
@Composable
private fun TaskTitleFieldPreview() {
    LifeOSTheme {
        TaskTitleField(title = "Sunum hazırlığı", onTitleChanged = {})
    }
}

@Preview
@Composable
private fun TaskTitleFieldErrorPreview() {
    LifeOSTheme {
        TaskTitleField(title = "", onTitleChanged = {}, errorMessage = PlannerStrings.TITLE_REQUIRED_ERROR)
    }
}
