package com.lifeos.app.features.planner.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
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
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.presentation.sections.CategorySection
import com.lifeos.app.features.planner.presentation.sections.PlannerCalendarSection
import com.lifeos.app.features.planner.presentation.sections.PlannerFab
import com.lifeos.app.features.planner.presentation.sections.PlannerHeader
import com.lifeos.app.features.planner.presentation.sections.ProgressSection
import com.lifeos.app.features.planner.presentation.sections.QuickActionsSection
import com.lifeos.app.features.planner.presentation.sections.TodayOverviewCard
import com.lifeos.app.features.planner.presentation.sections.TodayTasksSection
import com.lifeos.app.features.planner.presentation.sections.UpcomingTasksSection
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [PlannerViewModel] and forwards its one-shot [PlannerAction]s to
 * navigation or a snackbar, per the same Route/Screen split every other
 * feature follows.
 *
 * The [DisposableEffect] below dispatches [PlannerEvent.ScreenResumed]
 * whenever this screen returns to the foreground (e.g. the user creates or
 * deletes a task in Create Task/Task Detail, then navigates back here) —
 * the exact same mechanism [com.lifeos.app.features.home.presentation.HomeRoute]
 * already uses, for the same reason: [PlannerViewModel] persists across
 * navigation rather than being recreated, so without this, the Dashboard
 * would keep showing stale data until the process restarted.
 */
@Composable
fun PlannerRoute(
    onNavigateToProfile: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: PlannerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            PlannerAction.NavigateToProfile -> onNavigateToProfile()
            is PlannerAction.NavigateToTaskDetail -> onNavigateToTaskDetail(action.taskId)
            PlannerAction.NavigateToCreateTask -> onNavigateToCreateTask()
            PlannerAction.NavigateToCalendar -> onNavigateToCalendar()
            is PlannerAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(PlannerEvent.ScreenResumed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PlannerScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/**
 * The stateless, previewable screen. Composed entirely from section
 * composables (per this feature's "no single huge screen" rule) inside a
 * [LazyColumn].
 */
@Composable
private fun PlannerScreen(
    uiState: PlannerUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (PlannerEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            PlannerFab(onClick = { onEvent(PlannerEvent.CreateTaskClicked) })
        },
    ) { paddingValues ->
        val dashboard = uiState.dashboard
        when {
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(PlannerEvent.RetryClicked) },
            )
            uiState.isLoading || dashboard == null -> SkeletonListContent(
                count = LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            dashboard.todayTasks.isEmpty() && dashboard.upcomingTasks.isEmpty() -> EmptyState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                title = PlannerStrings.EMPTY_TITLE,
                description = PlannerStrings.EMPTY_DESCRIPTION,
                icon = Icons.Filled.EventNote,
            )
            else -> PlannerContent(
                uiState = uiState,
                dashboard = dashboard,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun PlannerContent(
    uiState: PlannerUiState,
    dashboard: PlannerDashboard,
    onEvent: (PlannerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCategory = uiState.selectedCategory
    val filteredTodayTasks = dashboard.todayTasks.filterByCategory(selectedCategory)
    val filteredUpcomingTasks = dashboard.upcomingTasks.filterByCategory(selectedCategory)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = LifeOSSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xl),
    ) {
        item {
            PlannerHeader(
                dateLabel = AppDateFormatter.toDayMonthWeekdayLabel(dashboard.date),
                onSettingsClick = { onEvent(PlannerEvent.SettingsClicked) },
            )
        }
        item {
            TodayOverviewCard(
                overview = dashboard.overview,
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
        item {
            ProgressSection(
                message = dashboard.aiInsightMessage,
                onUpdatePlanClick = { onEvent(PlannerEvent.UpdatePlanClicked) },
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
        item {
            PlannerCalendarSection(
                calendar = dashboard.calendar,
                onPreviousMonthClick = { onEvent(PlannerEvent.PreviousMonthClicked) },
                onNextMonthClick = { onEvent(PlannerEvent.NextMonthClicked) },
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
        item {
            CategorySection(
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> onEvent(PlannerEvent.CategorySelected(category)) },
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
        item {
            TodayTasksSection(
                tasks = filteredTodayTasks,
                onTaskToggled = { taskId -> onEvent(PlannerEvent.TaskToggled(taskId)) },
                onTaskClicked = { taskId -> onEvent(PlannerEvent.TaskClicked(taskId)) },
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
        item {
            UpcomingTasksSection(
                tasks = filteredUpcomingTasks,
                onTaskClicked = { taskId -> onEvent(PlannerEvent.TaskClicked(taskId)) },
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
        item {
            QuickActionsSection(
                onActionClick = { action -> onEvent(PlannerEvent.QuickActionClicked(action)) },
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
            )
        }
    }
}

/** "Quick Categories" filtering — presentation-layer derived state, never mutating the dashboard's own lists. */
private fun List<Task>.filterByCategory(category: TaskCategory?): List<Task> =
    if (category == null) this else filter { it.category == category }

private const val LOADING_SKELETON_COUNT = 4

@Preview
@Composable
private fun PlannerScreenLoadingPreview() {
    LifeOSTheme {
        PlannerScreen(
            uiState = PlannerUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun PlannerScreenErrorPreview() {
    LifeOSTheme {
        PlannerScreen(
            uiState = PlannerUiState(isLoading = false, errorMessage = PlannerStrings.LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun PlannerScreenEmptyPreview() {
    LifeOSTheme {
        val dashboard = FakePlannerDataSource().getDashboard().copy(todayTasks = emptyList(), upcomingTasks = emptyList())
        PlannerScreen(
            uiState = PlannerUiState(isLoading = false, dashboard = dashboard),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** The complete, loaded Planner screen — reuses [FakePlannerDataSource] directly. */
@Preview
@Composable
private fun PlannerScreenPreview() {
    LifeOSTheme {
        PlannerScreen(
            uiState = PlannerUiState(isLoading = false, dashboard = FakePlannerDataSource().getDashboard()),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
