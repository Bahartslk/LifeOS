package com.lifeos.app.features.travel.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppNotesField
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.travel.data.datasource.FakeTripGenerationDataSource
import com.lifeos.app.features.travel.domain.model.AccommodationPreference
import com.lifeos.app.features.travel.domain.model.TransportationType
import com.lifeos.app.features.travel.domain.model.TravelCompanions
import com.lifeos.app.features.travel.domain.model.TravelStyle
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripGenerationRequest
import com.lifeos.app.features.travel.presentation.sections.AccommodationSection
import com.lifeos.app.features.travel.presentation.sections.ActionButtons
import com.lifeos.app.features.travel.presentation.sections.AiGenerationCard
import com.lifeos.app.features.travel.presentation.sections.CompanionSection
import com.lifeos.app.features.travel.presentation.sections.DestinationSection
import com.lifeos.app.features.travel.presentation.sections.GeneratedTripPreview
import com.lifeos.app.features.travel.presentation.sections.TransportationSection
import com.lifeos.app.features.travel.presentation.sections.TravelBudgetInputSection
import com.lifeos.app.features.travel.presentation.sections.TravelDatesSection
import com.lifeos.app.features.travel.presentation.sections.TravelStyleSection
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [CreateTripViewModel] and forwards its one-shot
 * [CreateTripAction]s to navigation or a snackbar, per the same Route/Screen
 * split established by every other feature.
 */
@Composable
fun CreateTripRoute(
    onNavigateBack: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit,
    viewModel: CreateTripViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            CreateTripAction.NavigateBack -> onNavigateBack()
            is CreateTripAction.NavigateToTripDetail -> onNavigateToTripDetail(action.tripId)
            is CreateTripAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    CreateTripScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/**
 * The stateless, previewable screen. Whether [CreateTripUiState.generatedDraft]
 * is `null` switches between the input form and [GeneratedTripPreview] —
 * there is no separate loading/empty/error full-screen state: the form is
 * always the screen's natural "nothing generated yet" state, generation
 * progress is shown inline by [AiGenerationCard], and every failure
 * (validation, generation, save) surfaces as a snackbar rather than
 * replacing the form the user already filled in.
 */
@Composable
private fun CreateTripScreen(
    uiState: CreateTripUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CreateTripEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = TravelStrings.CREATE_TITLE,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = TravelStrings.BACK_CONTENT_DESCRIPTION,
                onNavigationClick = { onEvent(CreateTripEvent.BackClicked) },
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val draft = uiState.generatedDraft
        if (draft != null) {
            CreateTripPreviewContent(
                uiState = uiState,
                draft = draft,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        } else {
            CreateTripFormContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CreateTripFormContent(
    uiState: CreateTripUiState,
    onEvent: (CreateTripEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(LifeOSSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
    ) {
        item {
            Text(text = TravelStrings.CREATE_SUBTITLE, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            DestinationSection(
                country = uiState.destinationCountry,
                onCountryChanged = { onEvent(CreateTripEvent.CountryChanged(it)) },
                city = uiState.destinationCity,
                onCityChanged = { onEvent(CreateTripEvent.CityChanged(it)) },
            )
        }
        item {
            TravelDatesSection(
                startDate = uiState.startDate,
                onStartDateChanged = { onEvent(CreateTripEvent.StartDateChanged(it)) },
                endDate = uiState.endDate,
                onEndDateChanged = { onEvent(CreateTripEvent.EndDateChanged(it)) },
            )
        }
        item {
            TravelStyleSection(
                selectedStyle = uiState.travelStyle,
                onStyleSelected = { onEvent(CreateTripEvent.StyleSelected(it)) },
            )
        }
        item {
            TravelBudgetInputSection(
                budgetAmountText = uiState.budgetAmountText,
                onBudgetAmountChanged = { onEvent(CreateTripEvent.BudgetAmountChanged(it)) },
            )
        }
        item {
            CompanionSection(
                selectedCompanions = uiState.companions,
                onCompanionsSelected = { onEvent(CreateTripEvent.CompanionsSelected(it)) },
            )
        }
        item {
            TransportationSection(
                selectedTransportation = uiState.transportation,
                onTransportationSelected = { onEvent(CreateTripEvent.TransportationSelected(it)) },
            )
        }
        item {
            AccommodationSection(
                selectedPreference = uiState.accommodationPreference,
                onPreferenceSelected = { onEvent(CreateTripEvent.AccommodationPreferenceSelected(it)) },
            )
        }
        item {
            AppNotesField(
                notes = uiState.additionalNotes,
                onNotesChanged = { onEvent(CreateTripEvent.NotesChanged(it)) },
                label = TravelStrings.CREATE_NOTES_LABEL,
                placeholder = TravelStrings.CREATE_NOTES_PLACEHOLDER,
            )
        }
        item {
            AiGenerationCard(
                isGenerating = uiState.isGenerating,
                onGenerateClicked = { onEvent(CreateTripEvent.GenerateClicked) },
            )
        }
    }
}

@Composable
private fun CreateTripPreviewContent(
    uiState: CreateTripUiState,
    draft: TripDetail,
    onEvent: (CreateTripEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(LifeOSSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
    ) {
        item {
            GeneratedTripPreview(
                detail = draft,
                expandedDayNumbers = uiState.expandedDayNumbers,
                onDayExpandToggle = { onEvent(CreateTripEvent.DayExpandToggled(it)) },
                onPackingItemToggled = { onEvent(CreateTripEvent.PackingItemToggled(it)) },
            )
        }
        item {
            if (uiState.isGenerating) {
                AiGenerationCard(isGenerating = true, onGenerateClicked = {})
            } else {
                ActionButtons(
                    onEditClicked = { onEvent(CreateTripEvent.EditClicked) },
                    onRegenerateClicked = { onEvent(CreateTripEvent.RegenerateClicked) },
                    onSaveClicked = { onEvent(CreateTripEvent.SaveClicked) },
                    isSaving = uiState.isSaving,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CreateTripScreenFormPreview() {
    LifeOSTheme {
        CreateTripScreen(
            uiState = CreateTripUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun CreateTripScreenGeneratingPreview() {
    LifeOSTheme {
        CreateTripScreen(
            uiState = CreateTripUiState(
                destinationCountry = "Japonya",
                destinationCity = "Kyoto",
                startDate = "15.09.2026",
                endDate = "22.09.2026",
                budgetAmountText = "15000",
                isGenerating = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** The complete Generated Preview state — reuses [FakeTripGenerationDataSource] directly. */
@Preview
@Composable
private fun CreateTripScreenPreviewModePreview() {
    LifeOSTheme {
        val draft = FakeTripGenerationDataSource().generateTripDetail(
            request = TripGenerationRequest(
                destinationCity = "Kyoto",
                destinationCountry = "Japonya",
                startDateLabel = "15.09.2026",
                endDateLabel = "22.09.2026",
                travelStyle = TravelStyle.ADVENTURE,
                budgetAmount = 15000,
                companions = TravelCompanions.COUPLE,
                transportation = TransportationType.FLIGHT,
                accommodationPreference = AccommodationPreference.BOUTIQUE,
                additionalNotes = "",
            ),
            tripId = "trip-preview",
        )
        CreateTripScreen(
            uiState = CreateTripUiState(generatedDraft = draft),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
