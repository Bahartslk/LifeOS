package com.lifeos.app.features.travel.presentation

import com.lifeos.app.features.travel.domain.model.TravelListData

/**
 * [isSearchBarVisible]/[isFilterRowVisible] default to `false` to match
 * travel-list.png's default state exactly (neither is visible until the
 * header's search/filter icon is tapped) — see
 * [TravelEvent.SearchIconClicked]/[TravelEvent.FilterIconClicked].
 *
 * [searchQuery] and [selectedFilter] are held here per this task's "Search
 * field (UI only)" / "Filter chips (UI only)" scope: captured as real state,
 * not yet wired to actually filter [data] — see this feature's technical
 * debt note.
 */
data class TravelUiState(
    val isLoading: Boolean = true,
    val isSearchBarVisible: Boolean = false,
    val isFilterRowVisible: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: TravelFilter = TravelFilter.ALL,
    val data: TravelListData? = null,
    val errorMessage: String? = null,
)

enum class TravelFilter {
    ALL,
    UPCOMING,
    PAST,
}

sealed interface TravelEvent {
    /** Dispatched by `TravelRoute`'s `DisposableEffect` on `ON_RESUME` — see [TravelViewModel]'s KDoc. */
    data object ScreenResumed : TravelEvent
    data object RetryClicked : TravelEvent
    data object SearchIconClicked : TravelEvent
    data object FilterIconClicked : TravelEvent
    data class SearchQueryChanged(val query: String) : TravelEvent
    data class FilterSelected(val filter: TravelFilter) : TravelEvent
    data class TripClicked(val tripId: String) : TravelEvent
    data class TripOptionsClicked(val tripId: String) : TravelEvent
    data object ViewArchivedTripsClicked : TravelEvent
    data object CreateTripClicked : TravelEvent
}

sealed interface TravelAction {
    data class NavigateToTripDetail(val tripId: String) : TravelAction
    data object NavigateToCreateTravel : TravelAction
    data class ShowMessage(val message: String) : TravelAction
}
