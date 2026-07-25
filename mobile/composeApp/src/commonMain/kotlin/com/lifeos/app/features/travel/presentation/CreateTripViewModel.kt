package com.lifeos.app.features.travel.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.travel.domain.model.TripGenerationRequest
import com.lifeos.app.features.travel.domain.usecase.GenerateTripUseCase
import com.lifeos.app.features.travel.domain.usecase.SaveTripUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * "Create Travel (AI)"'s own ViewModel, alongside [TravelViewModel] and
 * [TravelDetailViewModel] in the same feature module — extending
 * `features/travel/` rather than a new module, per this task's scope.
 *
 * Depends only on [GenerateTripUseCase] and [SaveTripUseCase] — both of
 * which depend on repository *interfaces*
 * ([TripGenerationRepository][com.lifeos.app.features.travel.domain.repository.TripGenerationRepository],
 * [TravelRepository][com.lifeos.app.features.travel.domain.repository.TravelRepository]).
 * This class never imports anything from `data`, and never constructs
 * generated content itself — every piece of the generated draft's Turkish
 * copy lives in [FakeTripGenerationDataSource][com.lifeos.app.features.travel.data.datasource.FakeTripGenerationDataSource],
 * per this task's explicit "do not hardcode generated content inside the
 * ViewModel" rule.
 */
class CreateTripViewModel(
    private val generateTrip: GenerateTripUseCase,
    private val saveTrip: SaveTripUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    private val _actions = Channel<CreateTripAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: CreateTripEvent) {
        when (event) {
            CreateTripEvent.BackClicked -> sendAction(CreateTripAction.NavigateBack)
            is CreateTripEvent.CountryChanged -> update { it.copy(destinationCountry = event.value) }
            is CreateTripEvent.CityChanged -> update { it.copy(destinationCity = event.value) }
            is CreateTripEvent.StartDateChanged -> update { it.copy(startDate = event.value) }
            is CreateTripEvent.EndDateChanged -> update { it.copy(endDate = event.value) }
            is CreateTripEvent.StyleSelected -> update { it.copy(travelStyle = event.style) }
            is CreateTripEvent.BudgetAmountChanged -> update { it.copy(budgetAmountText = event.value) }
            is CreateTripEvent.CompanionsSelected -> update { it.copy(companions = event.companions) }
            is CreateTripEvent.TransportationSelected -> update { it.copy(transportation = event.transportation) }
            is CreateTripEvent.AccommodationPreferenceSelected -> {
                update { it.copy(accommodationPreference = event.preference) }
            }
            is CreateTripEvent.NotesChanged -> update { it.copy(additionalNotes = event.value) }
            CreateTripEvent.GenerateClicked -> generateDraft()
            CreateTripEvent.RegenerateClicked -> generateDraft()
            CreateTripEvent.EditClicked -> update { it.copy(generatedDraft = null) }
            CreateTripEvent.SaveClicked -> saveDraft()
            is CreateTripEvent.DayExpandToggled -> toggleDayExpanded(event.dayNumber)
            is CreateTripEvent.PackingItemToggled -> togglePackingItem(event.itemId)
        }
    }

    private fun generateDraft() {
        val request = _uiState.value.toGenerationRequestOrNull()
        if (request == null) {
            sendAction(CreateTripAction.ShowMessage(TravelStrings.CREATE_VALIDATION_ERROR_MESSAGE))
            return
        }

        viewModelScope.launch {
            update { it.copy(isGenerating = true) }
            generateTrip(request)
                .onSuccess { detail ->
                    update {
                        it.copy(isGenerating = false, generatedDraft = detail, expandedDayNumbers = emptySet())
                    }
                }
                .onFailure {
                    update { it.copy(isGenerating = false) }
                    sendAction(CreateTripAction.ShowMessage(TravelStrings.CREATE_GENERATION_ERROR_MESSAGE))
                }
        }
    }

    private fun saveDraft() {
        val draft = _uiState.value.generatedDraft ?: return

        viewModelScope.launch {
            update { it.copy(isSaving = true) }
            saveTrip(draft)
                .onSuccess { savedTripId ->
                    update { it.copy(isSaving = false) }
                    sendAction(CreateTripAction.NavigateToTripDetail(savedTripId))
                }
                .onFailure {
                    update { it.copy(isSaving = false) }
                    sendAction(CreateTripAction.ShowMessage(TravelStrings.CREATE_SAVE_ERROR_MESSAGE))
                }
        }
    }

    private fun toggleDayExpanded(dayNumber: Int) {
        val current = _uiState.value.expandedDayNumbers
        val updated = if (dayNumber in current) current - dayNumber else current + dayNumber
        update { it.copy(expandedDayNumbers = updated) }
    }

    private fun togglePackingItem(itemId: String) {
        val draft = _uiState.value.generatedDraft ?: return
        val updatedCategories = draft.packingCategories.map { category ->
            category.copy(
                items = category.items.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                },
            )
        }
        update { it.copy(generatedDraft = draft.copy(packingCategories = updatedCategories)) }
    }

    private fun update(transform: (CreateTripUiState) -> CreateTripUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun sendAction(action: CreateTripAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}

/**
 * `null` when any required field is missing/invalid — country, city, both
 * dates, and a positive numeric budget. [CreateTripViewModel] uses this to
 * decide whether to call [GenerateTripUseCase] at all or surface
 * [TravelStrings.CREATE_VALIDATION_ERROR_MESSAGE] instead.
 */
private fun CreateTripUiState.toGenerationRequestOrNull(): TripGenerationRequest? {
    val budgetAmount = budgetAmountText.toIntOrNull() ?: return null
    if (destinationCountry.isBlank() || destinationCity.isBlank()) return null
    if (startDate.isBlank() || endDate.isBlank()) return null
    if (budgetAmount <= 0) return null

    return TripGenerationRequest(
        destinationCity = destinationCity,
        destinationCountry = destinationCountry,
        startDateLabel = startDate,
        endDateLabel = endDate,
        travelStyle = travelStyle,
        budgetAmount = budgetAmount,
        companions = companions,
        transportation = transportation,
        accommodationPreference = accommodationPreference,
        additionalNotes = additionalNotes,
    )
}
