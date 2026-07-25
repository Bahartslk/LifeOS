package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The screen title with search/filter actions (travel-list.png: "World
 * Journeys" / "Curated experiences and future memories."). Composes
 * [AppTopBar] for the title row plus a subtitle line — no new component
 * needed, only [AppTopBar]'s existing `actions` slot.
 */
@Composable
fun TravelHeader(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppTopBar(
            title = TravelStrings.TITLE,
            actions = {
                IconButton(onClick = onSearchClick) {
                    AppIcon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = TravelStrings.SEARCH_ACTION_DESCRIPTION,
                    )
                }
                IconButton(onClick = onFilterClick) {
                    AppIcon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = TravelStrings.FILTER_ACTION_DESCRIPTION,
                    )
                }
            },
        )
        Text(
            text = TravelStrings.SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
        )
    }
}

@Preview
@Composable
private fun TravelHeaderPreview() {
    LifeOSTheme {
        TravelHeader(onSearchClick = {}, onFilterClick = {})
    }
}
