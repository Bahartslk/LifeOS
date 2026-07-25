package com.lifeos.app.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.home.data.datasource.HomeDataSource
import com.lifeos.app.features.home.domain.model.GreetingInfo
import com.lifeos.app.features.home.domain.model.HomeDashboard
import com.lifeos.app.features.home.domain.model.HubHighlight
import com.lifeos.app.features.home.domain.model.HubHighlightType
import com.lifeos.app.features.home.domain.model.IntelligentHubSummary
import com.lifeos.app.features.home.domain.model.OverviewStats
import com.lifeos.app.features.home.domain.model.PriorityTask
import com.lifeos.app.features.home.domain.model.TaskTrend
import com.lifeos.app.features.home.domain.model.UpcomingJourney
import com.lifeos.app.features.home.presentation.sections.GreetingSection
import com.lifeos.app.features.home.presentation.sections.IntelligentHubCard
import com.lifeos.app.features.home.presentation.sections.OverviewSection
import com.lifeos.app.features.home.presentation.sections.QuickActionsSection
import com.lifeos.app.features.home.presentation.sections.TodaysPrioritiesSection
import com.lifeos.app.features.home.presentation.sections.UpcomingJourneySection
import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [HomeViewModel] and forwards its one-shot [HomeAction]s to
 * navigation callbacks or a snackbar, per the same Route/Screen split
 * established by the Authentication feature.
 *
 * The [DisposableEffect] below dispatches [HomeEvent.ScreenResumed]
 * whenever this screen returns to the foreground (e.g. the user creates,
 * completes, or deletes a task in Planner, then switches back to the Home
 * tab) — [HomeViewModel] persists across tab switches
 * ([com.lifeos.app.core.navigation.navigateToMainTab] saves/restores state
 * rather than recreating the ViewModel), so without this, Home would keep
 * showing stale Planner data until the process restarted. This is this
 * sprint's "Home should automatically reflect newly created/completed/
 * deleted tasks" requirement.
 */
@Composable
fun HomeRoute(
    onNavigateToTravel: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToAiChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            HomeAction.NavigateToTravel -> onNavigateToTravel()
            HomeAction.NavigateToPlanner -> onNavigateToPlanner()
            HomeAction.NavigateToAiChat -> onNavigateToAiChat()
            HomeAction.NavigateToProfile -> onNavigateToProfile()
            HomeAction.NavigateToCreateTask -> onNavigateToCreateTask()
            is HomeAction.NavigateToTaskDetail -> onNavigateToTaskDetail(action.taskId)
            is HomeAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(HomeEvent.ScreenResumed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/**
 * The stateless, previewable screen. Composed entirely from section
 * composables (per this feature's "no single large HomeScreen" rule) inside
 * a [LazyColumn], each section owning exactly one concern.
 */
@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HomeEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(HomeEvent.RetryClicked) },
            )
            uiState.isLoading || uiState.dashboard == null || uiState.overview == null -> SkeletonListContent(
                count = HOME_LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            else -> HomeContent(
                dashboard = uiState.dashboard,
                greetingMessage = uiState.greetingMessage.orEmpty(),
                priorities = uiState.priorities,
                overview = uiState.overview,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun HomeContent(
    dashboard: HomeDashboard,
    greetingMessage: String,
    priorities: List<PriorityTask>,
    overview: OverviewStats,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = LifeOSSpacing.lg,
            vertical = LifeOSSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xl),
    ) {
        item {
            GreetingSection(
                greetingMessage = greetingMessage,
                greeting = dashboard.greeting,
                onSettingsClick = { onEvent(HomeEvent.SettingsClicked) },
            )
        }
        item {
            IntelligentHubCard(
                highlights = dashboard.intelligentHub.highlights,
                onCompleteChecklistClick = { onEvent(HomeEvent.CompleteChecklistClicked) },
                onViewTripClick = { onEvent(HomeEvent.ViewTripClicked) },
            )
        }
        item {
            QuickActionsSection(
                onActionClick = { action -> onEvent(HomeEvent.QuickActionClicked(action)) },
            )
        }
        item {
            OverviewSection(
                stats = overview,
                onViewInsightsClick = { onEvent(HomeEvent.QuickActionClicked(HomeQuickAction.NEW_TASK)) },
            )
        }
        item {
            TodaysPrioritiesSection(
                tasks = priorities,
                onManageAllClick = { onEvent(HomeEvent.ManageAllPrioritiesClicked) },
                onTaskClick = { taskId -> onEvent(HomeEvent.PriorityTaskClicked(taskId)) },
            )
        }
        item {
            UpcomingJourneySection(
                journey = dashboard.upcomingJourney,
                onViewMapClick = { onEvent(HomeEvent.ViewTripClicked) },
                onPackingListClick = { onEvent(HomeEvent.CompleteChecklistClicked) },
                onCreateTripClick = { onEvent(HomeEvent.QuickActionClicked(HomeQuickAction.CREATE_TRIP)) },
            )
        }
    }
}

private const val HOME_LOADING_SKELETON_COUNT = 4

@Preview
@Composable
private fun HomeScreenLoadingPreview() {
    LifeOSTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenErrorPreview() {
    LifeOSTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = false, errorMessage = HomeStrings.LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/**
 * The complete, loaded Home screen — reuses [FakePlannerDataSource] for
 * tasks directly, so the preview never drifts from Planner's own real fake
 * data. [HomeDataSource] itself now returns an empty shell (Iteration 4's
 * backend integration — real highlights/journey come from Planner/Travel
 * via [HomeViewModel] at runtime, not from this class), so this preview
 * builds a representative [IntelligentHubSummary]/[UpcomingJourney]
 * directly — preview-only sample data, same as every other feature's
 * `@Preview`, never a production code path.
 */
@Preview
@Composable
private fun HomeScreenPreview() {
    LifeOSTheme {
        val plannerDashboard = FakePlannerDataSource().getDashboard()
        HomeScreen(
            uiState = HomeUiState(
                isLoading = false,
                greetingMessage = HomeStrings.GREETING_MORNING,
                dashboard = HomeDataSource().getDashboard().copy(
                    greeting = GreetingInfo(userFirstName = "Bahar", avatarUrl = null),
                    intelligentHub = IntelligentHubSummary(
                        highlights = listOf(
                            HubHighlight(HubHighlightType.TRIP, HomeStrings.hubTripHighlight(3)),
                            HubHighlight(HubHighlightType.TASK, HomeStrings.hubTaskHighlight(2)),
                        ),
                    ),
                    upcomingJourney = UpcomingJourney(
                        destinationName = "Kapadokya",
                        region = "Türkiye",
                        daysRemaining = 3,
                        weatherTemperatureCelsius = null,
                        flightCode = null,
                        flightGate = null,
                        flightDepartureLabel = null,
                        hotelName = null,
                        hotelRoomType = null,
                        coverImageUrl = "https://images.unsplash.com/photo-1641731538990-98de926df457?w=1200",
                    ),
                ),
                priorities = plannerDashboard.todayTasks.map { it.toPriorityTask() },
                overview = OverviewStats(
                    completedTaskCount = plannerDashboard.overview.completedTaskCount,
                    totalTaskCount = plannerDashboard.overview.totalTaskCount,
                    productivityPercent = plannerDashboard.overview.productivityPercent,
                    productivityDeltaPercent = null,
                    weeklyCompletionRatios = emptyList(),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** A returning user with no priorities and no upcoming trip yet — a real, expected state, not an error. */
@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    LifeOSTheme {
        val dashboard = HomeDataSource().getDashboard()
            .copy(greeting = GreetingInfo(userFirstName = "Bahar", avatarUrl = null))
        HomeScreen(
            uiState = HomeUiState(
                isLoading = false,
                greetingMessage = HomeStrings.GREETING_MORNING,
                dashboard = dashboard,
                priorities = emptyList(),
                overview = OverviewStats(
                    completedTaskCount = 0,
                    totalTaskCount = 0,
                    productivityPercent = 0,
                    productivityDeltaPercent = null,
                    weeklyCompletionRatios = emptyList(),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
