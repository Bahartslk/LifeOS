package com.lifeos.app.features.travel.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import com.lifeos.app.features.travel.domain.model.TripNotFoundException
import com.lifeos.app.features.travel.domain.usecase.GetTripDetailUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Travel Detail's own ViewModel, alongside [TravelViewModel] in the same
 * feature module — extending `features/travel/` rather than a new module,
 * per this task's scope. [tripId] is supplied at creation time (via Koin's
 * parameter injection in `di/TravelModule.kt`), the same way a `NavHost`
 * hands a route argument to whichever screen owns it.
 *
 * Reuses Planner's existing [GetPlannerDashboardUseCase] directly for
 * [TravelDetailUiState.relatedTasks] — no new Planner use case, and
 * [com.lifeos.app.features.travel.domain.repository.TravelRepository]
 * never depends on [com.lifeos.app.features.planner.domain.repository.PlannerRepository]:
 * the two are composed here, at the ViewModel layer, exactly the same
 * architecture the Home integration already established (`PlannerRepository
 * -> UseCases -> ViewModel -> UI`, merged after both fetches complete, no
 * repository-to-repository dependency).
 */
class TravelDetailViewModel(
    private val tripId: String,
    private val getTripDetail: GetTripDetailUseCase,
    private val getPlannerDashboard: GetPlannerDashboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelDetailUiState())
    val uiState: StateFlow<TravelDetailUiState> = _uiState.asStateFlow()

    private val _actions = Channel<TravelDetailAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadDetail()
    }

    fun onEvent(event: TravelDetailEvent) {
        when (event) {
            TravelDetailEvent.BackClicked -> sendAction(TravelDetailAction.NavigateBack)
            TravelDetailEvent.RetryClicked -> loadDetail()
            TravelDetailEvent.ShareClicked -> showMessage(TravelStrings.SHARE_COMING_SOON)
            TravelDetailEvent.ManageBookingClicked -> showMessage(TravelStrings.MANAGE_BOOKING_COMING_SOON)
            TravelDetailEvent.ViewFullPackingListClicked -> showMessage(TravelStrings.PACKING_VIEW_FULL_LIST)
            is TravelDetailEvent.DocumentClicked -> showMessage(TravelStrings.DOCUMENT_COMING_SOON)
            is TravelDetailEvent.DayExpandToggled -> toggleDayExpanded(event.dayNumber)
            is TravelDetailEvent.PackingItemToggled -> togglePackingItem(event.itemId)
            is TravelDetailEvent.NotesChanged -> {
                _uiState.value = _uiState.value.copy(notesText = event.text)
            }
            is TravelDetailEvent.TaskClicked -> sendAction(TravelDetailAction.NavigateToTaskDetail(event.taskId))
            TravelDetailEvent.AddTaskClicked -> sendAction(TravelDetailAction.NavigateToCreateTask)
        }
    }

    private fun toggleDayExpanded(dayNumber: Int) {
        val current = _uiState.value.expandedDayNumbers
        val updated = if (dayNumber in current) current - dayNumber else current + dayNumber
        _uiState.value = _uiState.value.copy(expandedDayNumbers = updated)
    }

    /**
     * Flips one packing item's checked state. Purely an in-memory UI-state
     * transform (no repository call) per this sprint's scope — see this
     * feature's technical-debt note on persisting packing progress.
     */
    private fun togglePackingItem(itemId: String) {
        val detail = _uiState.value.detail ?: return
        val updatedCategories = detail.packingCategories.map { category ->
            category.copy(
                items = category.items.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                },
            )
        }
        _uiState.value = _uiState.value.copy(detail = detail.copy(packingCategories = updatedCategories))
    }

    /**
     * Fetches Travel's own [getTripDetail] and Planner's [getPlannerDashboard]
     * concurrently — independent calls, no reason to serialize them. Unlike
     * Home's merge (where both fetches are equally central and a Planner
     * failure fails the whole screen), a Planner failure here only empties
     * [TravelDetailUiState.relatedTasks] — trip preparation tasks are a
     * supplementary section, not core Travel content, so this screen stays
     * fully usable even if Planner is unreachable.
     */
    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isNotFound = false)
            coroutineScope {
                val detailDeferred = async { getTripDetail(tripId) }
                val plannerDeferred = async { getPlannerDashboard() }
                val detailResult = detailDeferred.await()
                val plannerResult = plannerDeferred.await()

                detailResult
                    .onSuccess { detail ->
                        val relatedTasks = plannerResult.getOrNull()
                            ?.let { it.todayTasks + it.upcomingTasks }
                            ?.filter { it.source == TaskSource.TRAVEL }
                            .orEmpty()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            detail = detail,
                            notesText = detail.notes,
                            relatedTasks = relatedTasks,
                        )
                    }
                    .onFailure { throwable ->
                        _uiState.value = if (throwable is TripNotFoundException) {
                            _uiState.value.copy(isLoading = false, isNotFound = true)
                        } else {
                            _uiState.value.copy(isLoading = false, errorMessage = TravelStrings.DETAIL_LOAD_ERROR_MESSAGE)
                        }
                    }
            }
        }
    }

    private fun showMessage(message: String) {
        sendAction(TravelDetailAction.ShowMessage(message))
    }

    private fun sendAction(action: TravelDetailAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
