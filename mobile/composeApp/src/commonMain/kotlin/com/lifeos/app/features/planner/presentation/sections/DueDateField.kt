package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Due Date" (Create Task's requirement) — still a free-text field, not a
 * real date picker (no visual/interaction change from this project's
 * architectural date refactor): no other feature in this codebase has a
 * picker either ([com.lifeos.app.features.travel.presentation.sections.TravelDatesSection]
 * is the same plain-text convention). What changed is what happens to the
 * text after it's typed — [com.lifeos.app.features.planner.presentation.CreateTaskViewModel]
 * now parses it via [com.lifeos.app.core.date.AppDateParser] into a real
 * [com.lifeos.app.features.planner.domain.model.TaskDueDate] before saving,
 * surfacing an unparseable value as the same field-level [errorMessage]
 * this composable already supported.
 */
@Composable
fun DueDateField(
    dueDate: String,
    onDueDateChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = dueDate,
            onValueChange = onDueDateChanged,
            label = PlannerStrings.DUE_DATE_LABEL,
            placeholder = PlannerStrings.DUE_DATE_FIELD_PLACEHOLDER,
            leadingIcon = Icons.Filled.CalendarToday,
            isError = errorMessage != null,
            supportingText = errorMessage,
        )
    }
}

@Preview
@Composable
private fun DueDateFieldPreview() {
    LifeOSTheme {
        DueDateField(dueDate = "Yarın, 14:00", onDueDateChanged = {})
    }
}

@Preview
@Composable
private fun DueDateFieldErrorPreview() {
    LifeOSTheme {
        DueDateField(dueDate = "", onDueDateChanged = {}, errorMessage = PlannerStrings.DUE_DATE_REQUIRED_ERROR)
    }
}
