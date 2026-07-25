package com.lifeos.app.features.travel.presentation

import com.lifeos.app.features.travel.domain.model.AccommodationPreference
import com.lifeos.app.features.travel.domain.model.TransportationType
import com.lifeos.app.features.travel.domain.model.TravelCompanions
import com.lifeos.app.features.travel.domain.model.TravelStyle
import com.lifeos.app.features.travel.domain.model.TripDetail

/**
 * All form fields default to Stitch's implied "nothing chosen yet" state,
 * except the four single-choice pickers ([travelStyle]/[companions]/
 * [transportation]/[accommodationPreference]) — an [OptionChipRow][com.lifeos.app.core.designsystem.components.OptionChipRow]
 * always needs exactly one option selected, so each defaults to its first,
 * most common enum entry rather than being nullable.
 *
 * [generatedDraft] being non-null is what switches
 * [CreateTripScreen][com.lifeos.app.features.travel.presentation.CreateTripRoute]
 * from the form into [GeneratedTripPreview][com.lifeos.app.features.travel.presentation.sections.GeneratedTripPreview]
 * mode (a plain null-check in `CreateTripScreen.kt`, which also smart-casts
 * it to a non-null [TripDetail] for the child composables) — there is no
 * separate boolean flag to keep in sync with it.
 * [expandedDayNumbers] mirrors the same UI-only concept
 * [TravelDetailUiState] already established for the reused [TimelineSection][com.lifeos.app.features.travel.presentation.sections.TimelineSection].
 *
 * There is no `errorMessage` field: validation failures, generation
 * failures, and save failures are all transient and must never discard the
 * user's already-filled-in form, so — unlike [TravelUiState]/
 * [TravelDetailUiState], which replace their whole screen with [ErrorView][com.lifeos.app.core.designsystem.components.ErrorView]
 * on failure — every failure here surfaces as a one-shot
 * [CreateTripAction.ShowMessage] snackbar instead, leaving the form intact
 * for another attempt.
 */
data class CreateTripUiState(
    val destinationCountry: String = "",
    val destinationCity: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val travelStyle: TravelStyle = TravelStyle.RELAX,
    val budgetAmountText: String = "",
    val companions: TravelCompanions = TravelCompanions.SOLO,
    val transportation: TransportationType = TransportationType.FLIGHT,
    val accommodationPreference: AccommodationPreference = AccommodationPreference.HOTEL,
    val additionalNotes: String = "",
    val isGenerating: Boolean = false,
    val generatedDraft: TripDetail? = null,
    val expandedDayNumbers: Set<Int> = emptySet(),
    val isSaving: Boolean = false,
)

sealed interface CreateTripEvent {
    data object BackClicked : CreateTripEvent
    data class CountryChanged(val value: String) : CreateTripEvent
    data class CityChanged(val value: String) : CreateTripEvent
    data class StartDateChanged(val value: String) : CreateTripEvent
    data class EndDateChanged(val value: String) : CreateTripEvent
    data class StyleSelected(val style: TravelStyle) : CreateTripEvent
    data class BudgetAmountChanged(val value: String) : CreateTripEvent
    data class CompanionsSelected(val companions: TravelCompanions) : CreateTripEvent
    data class TransportationSelected(val transportation: TransportationType) : CreateTripEvent
    data class AccommodationPreferenceSelected(val preference: AccommodationPreference) : CreateTripEvent
    data class NotesChanged(val value: String) : CreateTripEvent
    data object GenerateClicked : CreateTripEvent
    data object EditClicked : CreateTripEvent
    data object RegenerateClicked : CreateTripEvent
    data object SaveClicked : CreateTripEvent
    data class DayExpandToggled(val dayNumber: Int) : CreateTripEvent
    data class PackingItemToggled(val itemId: String) : CreateTripEvent
}

sealed interface CreateTripAction {
    data object NavigateBack : CreateTripAction
    data class NavigateToTripDetail(val tripId: String) : CreateTripAction
    data class ShowMessage(val message: String) : CreateTripAction
}
