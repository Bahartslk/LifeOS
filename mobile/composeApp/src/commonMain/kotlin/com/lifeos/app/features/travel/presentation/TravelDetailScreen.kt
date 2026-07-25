package com.lifeos.app.features.travel.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.lifeos.app.core.designsystem.components.AppNotesField
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.travel.data.datasource.FakeTravelDataSource
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.presentation.sections.AiSummaryCard
import com.lifeos.app.features.travel.presentation.sections.BudgetSection
import com.lifeos.app.features.travel.presentation.sections.FlightSection
import com.lifeos.app.features.travel.presentation.sections.HeroSection
import com.lifeos.app.features.travel.presentation.sections.HotelSection
import com.lifeos.app.features.travel.presentation.sections.PackingChecklistSection
import com.lifeos.app.features.travel.presentation.sections.TimelineSection
import com.lifeos.app.features.travel.presentation.sections.TravelDocumentsSection
import com.lifeos.app.features.travel.presentation.sections.TravelTasksSection
import com.lifeos.app.features.travel.presentation.sections.WeatherSection
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [TravelDetailViewModel] (created with [tripId] via Koin
 * parameter injection) and forwards its one-shot [TravelDetailAction]s to
 * navigation or a snackbar, per the same Route/Screen split established by
 * every other feature.
 */
@Composable
fun TravelDetailRoute(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    viewModel: TravelDetailViewModel = koinViewModel(parameters = { parametersOf(tripId) }),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            TravelDetailAction.NavigateBack -> onNavigateBack()
            is TravelDetailAction.NavigateToTaskDetail -> onNavigateToTaskDetail(action.taskId)
            TravelDetailAction.NavigateToCreateTask -> onNavigateToCreateTask()
            is TravelDetailAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    TravelDetailScreen(
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
private fun TravelDetailScreen(
    uiState: TravelDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TravelDetailEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isNotFound -> EmptyState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                title = TravelStrings.DETAIL_EMPTY_TITLE,
                description = TravelStrings.DETAIL_EMPTY_DESCRIPTION,
                icon = Icons.Filled.SearchOff,
                action = {
                    AppOutlinedButton(
                        text = TravelStrings.BACK_CONTENT_DESCRIPTION,
                        onClick = { onEvent(TravelDetailEvent.BackClicked) },
                    )
                },
            )
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(TravelDetailEvent.RetryClicked) },
            )
            uiState.isLoading || uiState.detail == null -> SkeletonListContent(
                count = DETAIL_LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            else -> TravelDetailContent(
                uiState = uiState,
                detail = uiState.detail,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun TravelDetailContent(
    uiState: TravelDetailUiState,
    detail: TripDetail,
    onEvent: (TravelDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            HeroSection(
                trip = detail.trip,
                onBackClick = { onEvent(TravelDetailEvent.BackClicked) },
                onShareClick = { onEvent(TravelDetailEvent.ShareClicked) },
            )
        }
        item {
            SectionSpacer {
                AiSummaryCard(summary = detail.aiSummary)
            }
        }
        item {
            SectionSpacer {
                TimelineSection(
                    days = detail.itinerary,
                    expandedDayNumbers = uiState.expandedDayNumbers,
                    onDayExpandToggle = { day -> onEvent(TravelDetailEvent.DayExpandToggled(day)) },
                    onManageBookingClick = { onEvent(TravelDetailEvent.ManageBookingClicked) },
                )
            }
        }
        if (detail.flight != null) {
            item {
                SectionSpacer {
                    FlightSection(flight = detail.flight)
                }
            }
        }
        item {
            SectionSpacer {
                HotelSection(accommodation = detail.accommodation)
            }
        }
        item {
            SectionSpacer {
                WeatherSection(forecast = detail.weatherForecast)
            }
        }
        item {
            SectionSpacer {
                BudgetSection(budget = detail.budget)
            }
        }
        item {
            SectionSpacer {
                PackingChecklistSection(
                    categories = detail.packingCategories,
                    onItemToggled = { itemId -> onEvent(TravelDetailEvent.PackingItemToggled(itemId)) },
                    onViewFullListClick = { onEvent(TravelDetailEvent.ViewFullPackingListClicked) },
                )
            }
        }
        item {
            SectionSpacer {
                TravelTasksSection(
                    tasks = uiState.relatedTasks,
                    onTaskClick = { taskId -> onEvent(TravelDetailEvent.TaskClicked(taskId)) },
                    onAddTaskClick = { onEvent(TravelDetailEvent.AddTaskClicked) },
                )
            }
        }
        item {
            SectionSpacer {
                TravelDocumentsSection(
                    documents = detail.documents,
                    onDocumentClick = { type -> onEvent(TravelDetailEvent.DocumentClicked(type)) },
                )
            }
        }
        item {
            SectionSpacer(bottomPadding = LifeOSSpacing.xl) {
                AppNotesField(
                    notes = uiState.notesText,
                    onNotesChanged = { text -> onEvent(TravelDetailEvent.NotesChanged(text)) },
                    label = TravelStrings.NOTES_TITLE,
                    placeholder = TravelStrings.NOTES_PLACEHOLDER,
                )
            }
        }
    }
}

/** Applies this screen's consistent horizontal/vertical rhythm around one section. */
@Composable
private fun SectionSpacer(
    bottomPadding: Dp = LifeOSSpacing.lg,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.padding(
            start = LifeOSSpacing.lg,
            end = LifeOSSpacing.lg,
            top = LifeOSSpacing.lg,
            bottom = bottomPadding,
        ),
    ) {
        content()
    }
}

private const val DETAIL_LOADING_SKELETON_COUNT = 4

@Preview
@Composable
private fun TravelDetailScreenLoadingPreview() {
    LifeOSTheme {
        TravelDetailScreen(
            uiState = TravelDetailUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun TravelDetailScreenErrorPreview() {
    LifeOSTheme {
        TravelDetailScreen(
            uiState = TravelDetailUiState(isLoading = false, errorMessage = TravelStrings.DETAIL_LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun TravelDetailScreenEmptyPreview() {
    LifeOSTheme {
        TravelDetailScreen(
            uiState = TravelDetailUiState(isLoading = false, isNotFound = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/**
 * The complete, loaded Travel Detail screen — reuses [FakeTravelDataSource]
 * for trip content and [FakePlannerDataSource] for related tasks directly,
 * so the preview never drifts from either feature's real fake data (this
 * sprint's Planner integration).
 */
@Preview
@Composable
private fun TravelDetailScreenPreview() {
    LifeOSTheme {
        val detail = FakeTravelDataSource().getTripDetail("trip-cappadocia")
        val plannerDashboard = FakePlannerDataSource().getDashboard()
        TravelDetailScreen(
            uiState = TravelDetailUiState(
                isLoading = false,
                detail = detail,
                notesText = detail?.notes.orEmpty(),
                expandedDayNumbers = setOf(2),
                relatedTasks = (plannerDashboard.todayTasks + plannerDashboard.upcomingTasks)
                    .filter { it.source == TaskSource.TRAVEL },
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
