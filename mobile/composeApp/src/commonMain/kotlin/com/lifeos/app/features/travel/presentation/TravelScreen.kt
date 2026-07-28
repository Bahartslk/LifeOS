package com.lifeos.app.features.travel.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lifeos.app.core.designsystem.components.AppSecondaryButton
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.travel.data.datasource.FakeTravelDataSource
import com.lifeos.app.features.travel.domain.model.TravelListData
import com.lifeos.app.features.travel.domain.model.TravelStatistics
import com.lifeos.app.features.travel.presentation.sections.PastJourneysSection
import com.lifeos.app.features.travel.presentation.sections.TravelFab
import com.lifeos.app.features.travel.presentation.sections.TravelFilters
import com.lifeos.app.features.travel.presentation.sections.TravelHeader
import com.lifeos.app.features.travel.presentation.sections.TravelSearchBar
import com.lifeos.app.features.travel.presentation.sections.TravelStatisticsSection
import com.lifeos.app.features.travel.presentation.sections.UpcomingJourneysSection
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [TravelViewModel] and forwards its one-shot [TravelAction]s to
 * either navigation or a snackbar, per the same Route/Screen split
 * established by Authentication and Home. Trip taps and the FAB both
 * navigate to real screens now (Travel Detail and Create Travel (AI)).
 *
 * The [DisposableEffect] below dispatches [TravelEvent.ScreenResumed]
 * whenever this screen returns to the foreground (e.g. the user saves a new
 * trip in Create Travel (AI), then navigates back here) — the exact same
 * mechanism `PlannerRoute`/`HomeRoute` already use, for the same reason:
 * [TravelViewModel] persists across navigation rather than being recreated,
 * so without this, the list would keep showing its pre-save contents.
 */
@Composable
fun TravelRoute(
    onNavigateToTripDetail: (String) -> Unit,
    onNavigateToCreateTravel: () -> Unit,
    viewModel: TravelViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            is TravelAction.NavigateToTripDetail -> onNavigateToTripDetail(action.tripId)
            TravelAction.NavigateToCreateTravel -> onNavigateToCreateTravel()
            is TravelAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(TravelEvent.ScreenResumed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    TravelScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/**
 * The stateless, previewable screen. Composed entirely from section
 * composables (per this feature's "no single huge screen" rule).
 */
@Composable
private fun TravelScreen(
    uiState: TravelUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TravelEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            TravelFab(onClick = { onEvent(TravelEvent.CreateTripClicked) })
        },
    ) { paddingValues ->
        when {
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(TravelEvent.RetryClicked) },
            )
            uiState.isLoading || uiState.data == null -> SkeletonListContent(
                count = TRAVEL_LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            uiState.data.upcomingTrips.isEmpty() && uiState.data.pastTrips.isEmpty() -> EmptyState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                title = TravelStrings.EMPTY_TITLE,
                description = TravelStrings.EMPTY_DESCRIPTION,
                icon = Icons.Filled.FlightTakeoff,
                action = {
                    AppSecondaryButton(
                        text = TravelStrings.FAB_NEW_TRIP,
                        onClick = { onEvent(TravelEvent.CreateTripClicked) },
                    )
                },
            )
            else -> TravelContent(
                uiState = uiState,
                data = uiState.data,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun TravelContent(
    uiState: TravelUiState,
    data: TravelListData,
    onEvent: (TravelEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = LifeOSSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xl),
    ) {
        item {
            TravelHeader(
                onSearchClick = { onEvent(TravelEvent.SearchIconClicked) },
                onFilterClick = { onEvent(TravelEvent.FilterIconClicked) },
            )
        }
        if (uiState.isSearchBarVisible) {
            item {
                TravelSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(TravelEvent.SearchQueryChanged(it)) },
                )
            }
        }
        if (uiState.isFilterRowVisible) {
            item {
                TravelFilters(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { onEvent(TravelEvent.FilterSelected(it)) },
                )
            }
        }
        item {
            TravelStatisticsSection(
                statistics = data.statistics,
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
        if (data.upcomingTrips.isNotEmpty()) {
            item {
                UpcomingJourneysSection(
                    trips = data.upcomingTrips,
                    onTripClick = { tripId -> onEvent(TravelEvent.TripClicked(tripId)) },
                )
            }
        }
        item {
            PastJourneysSection(
                trips = data.pastTrips,
                archivedTripCount = data.archivedTripCount,
                archivedYearRangeLabel = data.archivedYearRangeLabel,
                onTripClick = { tripId -> onEvent(TravelEvent.TripClicked(tripId)) },
                onTripOptionsClick = { tripId -> onEvent(TravelEvent.TripOptionsClicked(tripId)) },
                onViewArchivedTripsClick = { onEvent(TravelEvent.ViewArchivedTripsClicked) },
            )
        }
    }
}

private const val TRAVEL_LOADING_SKELETON_COUNT = 3

@Preview
@Composable
private fun TravelScreenLoadingPreview() {
    LifeOSTheme {
        TravelScreen(
            uiState = TravelUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun TravelScreenErrorPreview() {
    LifeOSTheme {
        TravelScreen(
            uiState = TravelUiState(isLoading = false, errorMessage = TravelStrings.LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun TravelScreenEmptyPreview() {
    LifeOSTheme {
        TravelScreen(
            uiState = TravelUiState(
                isLoading = false,
                data = TravelListData(
                    upcomingTrips = emptyList(),
                    pastTrips = emptyList(),
                    statistics = TravelStatistics(totalTripCount = 0, countriesVisitedCount = 0, upcomingTripCount = 0),
                    archivedTripCount = 0,
                    archivedYearRangeLabel = "",
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** The complete, loaded Travel List screen — reuses [FakeTravelDataSource] directly. */
@Preview
@Composable
private fun TravelScreenPreview() {
    LifeOSTheme {
        TravelScreen(
            uiState = TravelUiState(isLoading = false, data = FakeTravelDataSource().getTravelList()),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
