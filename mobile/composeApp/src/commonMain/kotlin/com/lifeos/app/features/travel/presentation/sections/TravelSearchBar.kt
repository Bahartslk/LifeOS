package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.SearchField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The trip search input — hidden by default (matching travel-list.png,
 * which shows no search bar until requested) and revealed by
 * [TravelHeader]'s search icon. "UI only" per this task's scope: [query]
 * is held in state but not yet wired to filter the trip list.
 */
@Composable
fun TravelSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LifeOSSpacing.lg),
        placeholder = TravelStrings.SEARCH_PLACEHOLDER,
    )
}

@Preview
@Composable
private fun TravelSearchBarPreview() {
    LifeOSTheme {
        TravelSearchBar(query = "", onQueryChange = {})
    }
}
