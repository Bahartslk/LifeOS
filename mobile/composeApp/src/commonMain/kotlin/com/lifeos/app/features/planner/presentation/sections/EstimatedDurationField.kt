package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Estimated Duration" (Create Task's requirement) — a free-text label
 * (e.g. "45 dakika"), the same pre-formatted-string convention
 * [com.lifeos.app.features.planner.domain.model.Task.estimatedDurationLabel]
 * itself follows, rather than a numeric minutes field with its own unit
 * picker.
 */
@Composable
fun EstimatedDurationField(
    estimatedDuration: String,
    onEstimatedDurationChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = estimatedDuration,
            onValueChange = onEstimatedDurationChanged,
            label = PlannerStrings.ESTIMATED_DURATION_LABEL,
            placeholder = PlannerStrings.ESTIMATED_DURATION_PLACEHOLDER,
            leadingIcon = Icons.Filled.Timer,
        )
    }
}

@Preview
@Composable
private fun EstimatedDurationFieldPreview() {
    LifeOSTheme {
        EstimatedDurationField(estimatedDuration = "45 dakika", onEstimatedDurationChanged = {})
    }
}
