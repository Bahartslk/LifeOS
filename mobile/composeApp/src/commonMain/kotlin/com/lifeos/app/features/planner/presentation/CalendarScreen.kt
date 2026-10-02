package com.lifeos.app.features.planner.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.lifeos.app.core.date.AppDateFormatter
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.presentation.sections.CalendarGrid
import com.lifeos.app.features.planner.presentation.sections.CalendarHeader
import com.lifeos.app.features.planner.presentation.sections.MonthSelector
import com.lifeos.app.features.planner.presentation.sections.PlannerFab
import com.lifeos.app.features.planner.presentation.sections.SelectedDayAgenda
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [CalendarViewModel] and forwards its one-shot [CalendarAction]s
 * to navigation or a snackbar, per the same Route/Screen split every other
 * feature follows. Selecting a task opens the existing Task Detail screen;
 * the FAB opens the existing Create Task screen — Calendar introduces no
 * navigation destinations of its own beyond itself.
 *
 * The [DisposableEffect] below dispatches [CalendarEvent.ScreenResumed]
 * whenever this screen returns to the foreground — the same mechanism
 * `PlannerRoute` uses — so returning from Create Task/Task Detail shows the
 * calendar's current real tasks.
 */
@Composable
fun CalendarRoute(
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            CalendarAction.NavigateBack -> onNavigateBack()
            is CalendarAction.NavigateToTaskDetail -> onNavigateToTaskDetail(action.taskId)
            CalendarAction.NavigateToCreateTask -> onNavigateToCreateTask()
            is CalendarAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(CalendarEvent.ScreenResumed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CalendarScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/** The stateless, previewable screen. Composed entirely from section composables (per this feature's "no single huge screen" rule). */
@Composable
private fun CalendarScreen(
    uiState: CalendarUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CalendarEvent) -> Unit,
) {
    Scaffold(
        topBar = { CalendarHeader(onBackClick = { onEvent(CalendarEvent.BackClicked) }) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            PlannerFab(onClick = { onEvent(CalendarEvent.CreateTaskClicked) })
        },
    ) { paddingValues ->
        val calendar = uiState.calendar
        when {
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(CalendarEvent.RetryClicked) },
            )
            uiState.isLoading || calendar == null -> SkeletonListContent(
                count = LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            else -> CalendarContent(
                uiState = uiState,
                calendar = calendar,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    calendar: PlannerCalendarMonth,
    onEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(LifeOSSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
    ) {
        MonthSelector(
            monthLabel = calendar.monthLabel,
            onPreviousMonthClick = { onEvent(CalendarEvent.PreviousMonthClicked) },
            onNextMonthClick = { onEvent(CalendarEvent.NextMonthClicked) },
        )
        CalendarGrid(
            calendar = calendar,
            selectedDate = uiState.selectedDate,
            onDaySelected = { date -> onEvent(CalendarEvent.DaySelected(date)) },
        )
        val selectedDate = uiState.selectedDate ?: calendar.days.first { it.isCurrentMonth }.date
        SelectedDayAgenda(
            dayTitle = AppDateFormatter.toShortLabel(selectedDate),
            tasks = uiState.agendaTasks,
            isLoading = uiState.isAgendaLoading,
            onTaskClick = { taskId -> onEvent(CalendarEvent.TaskClicked(taskId)) },
        )
    }
}

private const val LOADING_SKELETON_COUNT = 4

@Preview
@Composable
private fun CalendarScreenLoadingPreview() {
    LifeOSTheme {
        CalendarScreen(
            uiState = CalendarUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun CalendarScreenErrorPreview() {
    LifeOSTheme {
        CalendarScreen(
            uiState = CalendarUiState(isLoading = false, errorMessage = PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** The complete, loaded Calendar screen — reuses [FakePlannerDataSource] directly. */
@Preview
@Composable
private fun CalendarScreenPreview() {
    LifeOSTheme {
        val dataSource = FakePlannerDataSource()
        val calendar = dataSource.calendarMonth(2024, 10)
        CalendarScreen(
            uiState = CalendarUiState(
                isLoading = false,
                calendar = calendar,
                selectedDate = LocalDate(2024, 10, 24),
                agendaTasks = dataSource.tasksForDay(LocalDate(2024, 10, 24)),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** A month with no matching fake tasks for the selected day — demonstrates the "Empty Day State." */
@Preview
@Composable
private fun CalendarScreenEmptyDayPreview() {
    LifeOSTheme {
        val dataSource = FakePlannerDataSource()
        CalendarScreen(
            uiState = CalendarUiState(
                isLoading = false,
                calendar = dataSource.calendarMonth(2024, 9),
                selectedDate = LocalDate(2024, 9, 1),
                agendaTasks = emptyList(),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun CalendarScreenAgendaLoadingPreview() {
    LifeOSTheme {
        val dataSource = FakePlannerDataSource()
        CalendarScreen(
            uiState = CalendarUiState(
                isLoading = false,
                calendar = dataSource.calendarMonth(2024, 10),
                selectedDate = LocalDate(2024, 10, 25),
                isAgendaLoading = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
