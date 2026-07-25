package com.lifeos.app.features.ai.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.ai.domain.model.AiTaskContext
import com.lifeos.app.features.ai.domain.model.AiTaskProgress
import com.lifeos.app.features.ai.domain.usecase.BuildAiTaskContextUseCase
import com.lifeos.app.features.ai.presentation.sections.AiAssistantHeader
import com.lifeos.app.features.ai.presentation.sections.AiProgressSummarySection
import com.lifeos.app.features.ai.presentation.sections.AiTaskListSection
import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [AiAssistantViewModel] and forwards its one-shot
 * [AiAssistantAction]s to navigation or a snackbar, per the same
 * Route/Screen split every other feature follows.
 *
 * The [DisposableEffect] mirrors [com.lifeos.app.features.home.presentation.HomeRoute]'s
 * exact resume-refresh pattern — see [AiAssistantViewModel]'s KDoc for why.
 */
@Composable
fun AiAssistantRoute(
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    viewModel: AiAssistantViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            is AiAssistantAction.NavigateToTaskDetail -> onNavigateToTaskDetail(action.taskId)
            AiAssistantAction.NavigateToCreateTask -> onNavigateToCreateTask()
            is AiAssistantAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(AiAssistantEvent.ScreenResumed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AiAssistantScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/** The stateless, previewable screen. Composed entirely from section composables (per this project's "no single huge screen" rule). */
@Composable
private fun AiAssistantScreen(
    uiState: AiAssistantUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (AiAssistantEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(AiAssistantEvent.RetryClicked) },
            )
            uiState.isLoading || uiState.context == null -> SkeletonListContent(
                count = LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            else -> AiAssistantContent(
                context = uiState.context,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun AiAssistantContent(
    context: AiTaskContext,
    onEvent: (AiAssistantEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = LifeOSSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xl),
    ) {
        item {
            AiAssistantHeader()
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = LifeOSSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
            ) {
                AiProgressSummarySection(progress = context.progress)
                AiTaskListSection(
                    title = AiAssistantStrings.TODAY_TASKS_TITLE,
                    tasks = context.todayTasks,
                    emptyMessage = AiAssistantStrings.TODAY_TASKS_EMPTY,
                    onTaskClick = { taskId -> onEvent(AiAssistantEvent.TaskClicked(taskId)) },
                )
                AiTaskListSection(
                    title = AiAssistantStrings.UPCOMING_TASKS_TITLE,
                    tasks = context.upcomingTasks,
                    emptyMessage = AiAssistantStrings.UPCOMING_TASKS_EMPTY,
                    onTaskClick = { taskId -> onEvent(AiAssistantEvent.TaskClicked(taskId)) },
                )
                AiTaskListSection(
                    title = AiAssistantStrings.HIGH_PRIORITY_TITLE,
                    tasks = context.highPriorityTasks,
                    emptyMessage = AiAssistantStrings.HIGH_PRIORITY_EMPTY,
                    onTaskClick = { taskId -> onEvent(AiAssistantEvent.TaskClicked(taskId)) },
                )
                AiTaskListSection(
                    title = AiAssistantStrings.OVERDUE_TITLE,
                    tasks = context.overdueTasks,
                    emptyMessage = AiAssistantStrings.OVERDUE_EMPTY,
                    onTaskClick = { taskId -> onEvent(AiAssistantEvent.TaskClicked(taskId)) },
                )
                AiTaskListSection(
                    title = AiAssistantStrings.TRAVEL_TASKS_TITLE,
                    tasks = context.travelTasks,
                    emptyMessage = AiAssistantStrings.TRAVEL_TASKS_EMPTY,
                    onTaskClick = { taskId -> onEvent(AiAssistantEvent.TaskClicked(taskId)) },
                )
                AppOutlinedButton(
                    text = AiAssistantStrings.ADD_TASK_ACTION,
                    onClick = { onEvent(AiAssistantEvent.AddTaskClicked) },
                )
            }
        }
    }
}

private const val LOADING_SKELETON_COUNT = 4

@Preview
@Composable
private fun AiAssistantScreenLoadingPreview() {
    LifeOSTheme {
        AiAssistantScreen(
            uiState = AiAssistantUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun AiAssistantScreenErrorPreview() {
    LifeOSTheme {
        AiAssistantScreen(
            uiState = AiAssistantUiState(isLoading = false, errorMessage = AiAssistantStrings.LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** The complete, loaded AI Assistant screen — reuses [FakePlannerDataSource] directly (through the real [BuildAiTaskContextUseCase]), so the preview never drifts from Planner's real fake data. */
@Preview
@Composable
private fun AiAssistantScreenPreview() {
    LifeOSTheme {
        val dashboard = FakePlannerDataSource().getDashboard()
        AiAssistantScreen(
            uiState = AiAssistantUiState(
                isLoading = false,
                context = BuildAiTaskContextUseCase()(dashboard),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** A context with no tasks in any category — a real, expected state, not an error. */
@Preview
@Composable
private fun AiAssistantScreenEmptyPreview() {
    LifeOSTheme {
        AiAssistantScreen(
            uiState = AiAssistantUiState(
                isLoading = false,
                context = AiTaskContext(
                    todayTasks = emptyList(),
                    upcomingTasks = emptyList(),
                    highPriorityTasks = emptyList(),
                    overdueTasks = emptyList(),
                    travelTasks = emptyList(),
                    progress = AiTaskProgress(completedCount = 0, totalCount = 0, completionPercent = 0),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
