package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.presentation.TravelFilter
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Trip filter chips — hidden by default (matching travel-list.png) and
 * revealed by [TravelHeader]'s filter icon. "UI only" per this task's
 * scope: [selectedFilter] is held in state but not yet wired to filter the
 * trip list. Uses Material 3's own [FilterChip] directly rather than a new
 * Design System component — its built-in selected/unselected treatment is
 * exactly what a filter chip needs, unlike [com.lifeos.app.core.designsystem.components.SuggestionChip]
 * (always-outlined, meant for tappable AI follow-up prompts).
 */
@Composable
fun TravelFilters(
    selectedFilter: TravelFilter,
    onFilterSelected: (TravelFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = LifeOSSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
    ) {
        TravelFilterChip(
            label = TravelStrings.FILTER_ALL,
            selected = selectedFilter == TravelFilter.ALL,
            onClick = { onFilterSelected(TravelFilter.ALL) },
        )
        TravelFilterChip(
            label = TravelStrings.FILTER_UPCOMING,
            selected = selectedFilter == TravelFilter.UPCOMING,
            onClick = { onFilterSelected(TravelFilter.UPCOMING) },
        )
        TravelFilterChip(
            label = TravelStrings.FILTER_PAST,
            selected = selectedFilter == TravelFilter.PAST,
            onClick = { onFilterSelected(TravelFilter.PAST) },
        )
    }
}

@Composable
private fun TravelFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Preview
@Composable
private fun TravelFiltersPreview() {
    LifeOSTheme {
        TravelFilters(selectedFilter = TravelFilter.ALL, onFilterSelected = {})
    }
}
