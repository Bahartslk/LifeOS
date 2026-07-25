package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * Renders one [SuggestionChip] per option, wrapping to a new line on narrow
 * widths — the single-choice picker used by "Create Travel (AI)"'s Travel
 * Style/Companions/Transportation/Accommodation Preference sections and by
 * Planner's Quick Categories filter. Originally feature-local to Travel;
 * promoted here once Planner needed the exact same
 * `FlowRow { options.forEach { SuggestionChip(...) } }` shape, per this
 * project's "only extract a Design System component once it's reused by at
 * least two features" rule.
 *
 * [selectedOption] may itself be a nullable type (e.g. `TaskCategory?`) for
 * callers that need an "all/none selected" option — plain `==` comparison
 * against `null` already works correctly, no special-casing needed here.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> OptionChipRow(
    options: List<T>,
    selectedOption: T,
    labelFor: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
    ) {
        options.forEach { option ->
            SuggestionChip(
                label = labelFor(option),
                selected = option == selectedOption,
                onClick = { onOptionSelected(option) },
            )
        }
    }
}
