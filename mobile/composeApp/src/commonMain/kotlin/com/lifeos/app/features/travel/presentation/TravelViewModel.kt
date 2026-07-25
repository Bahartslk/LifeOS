package com.lifeos.app.features.travel.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.travel.domain.usecase.GetTravelListUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * [TravelEvent.TripClicked] navigates to the real Travel Detail screen, and
 * [TravelEvent.CreateTripClicked] now navigates to the real Create Travel
 * (AI) screen (this sprint's feature) — no more "coming soon" message for
 * either.
 */
class TravelViewModel(
    private val getTravelList: GetTravelListUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelUiState())
    val uiState: StateFlow<TravelUiState> = _uiState.asStateFlow()

    private val _actions = Channel<TravelAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadTravelList()
    }

    fun onEvent(event: TravelEvent) {
        when (event) {
            TravelEvent.RetryClicked -> loadTravelList()
            TravelEvent.SearchIconClicked -> toggleSearchBar()
            TravelEvent.FilterIconClicked -> toggleFilterRow()
            is TravelEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            }
            is TravelEvent.FilterSelected -> {
                _uiState.value = _uiState.value.copy(selectedFilter = event.filter)
            }
            is TravelEvent.TripClicked -> sendAction(TravelAction.NavigateToTripDetail(event.tripId))
            is TravelEvent.TripOptionsClicked -> showMessage(TravelStrings.TRIP_OPTIONS_COMING_SOON)
            TravelEvent.ViewArchivedTripsClicked -> showMessage(TravelStrings.TRIP_DETAIL_COMING_SOON)
            TravelEvent.CreateTripClicked -> sendAction(TravelAction.NavigateToCreateTravel)
        }
    }

    private fun toggleSearchBar() {
        _uiState.value = _uiState.value.copy(isSearchBarVisible = !_uiState.value.isSearchBarVisible)
    }

    private fun toggleFilterRow() {
        _uiState.value = _uiState.value.copy(isFilterRowVisible = !_uiState.value.isFilterRowVisible)
    }

    private fun loadTravelList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            getTravelList()
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(isLoading = false, data = data)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = TravelStrings.LOAD_ERROR_MESSAGE,
                    )
                }
        }
    }

    private fun showMessage(message: String) {
        sendAction(TravelAction.ShowMessage(message))
    }

    private fun sendAction(action: TravelAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
