package com.lifeos.app.features.planner.presentation

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
import com.lifeos.app.core.designsystem.components.ConfirmationDialog
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.presentation.sections.AttachmentSection
import com.lifeos.app.features.planner.presentation.sections.PrioritySection
import com.lifeos.app.features.planner.presentation.sections.RelatedAiSuggestionsSection
import com.lifeos.app.features.planner.presentation.sections.RelatedTripSection
import com.lifeos.app.features.planner.presentation.sections.SubtaskSection
import com.lifeos.app.features.planner.presentation.sections.TaskActionsSection
import com.lifeos.app.features.planner.presentation.sections.TaskCategorySection
import com.lifeos.app.features.planner.presentation.sections.TaskHeader
import com.lifeos.app.features.planner.presentation.sections.TaskInfoCard
import com.lifeos.app.features.planner.presentation.sections.TaskProgressSection
import com.lifeos.app.features.planner.presentation.sections.TimelineSection
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [TaskDetailViewModel] (created with [taskId] via Koin parameter
 * injection) and forwards its one-shot [TaskDetailAction]s to navigation or
 * a snackbar, mirroring [TravelDetailRoute]'s exact Route/Screen split.
 */
@Composable
fun TaskDetailRoute(
    taskId: String,
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = koinViewModel(parameters = { parametersOf(taskId) }),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            TaskDetailAction.NavigateBack -> onNavigateBack()
            is TaskDetailAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    TaskDetailScreen(
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
private fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TaskDetailEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isNotFound -> EmptyState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                title = PlannerStrings.DETAIL_EMPTY_TITLE,
                description = PlannerStrings.DETAIL_EMPTY_DESCRIPTION,
                icon = Icons.Filled.SearchOff,
                action = {
                    AppOutlinedButton(
                        text = PlannerStrings.BACK_CONTENT_DESCRIPTION,
                        onClick = { onEvent(TaskDetailEvent.BackClicked) },
                    )
                },
            )
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(TaskDetailEvent.RetryClicked) },
            )
            uiState.isLoading || uiState.detail == null -> SkeletonListContent(
                count = DETAIL_LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            else -> TaskDetailContent(
                uiState = uiState,
                detail = uiState.detail,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }

    if (uiState.isDeleteConfirmationVisible) {
        ConfirmationDialog(
            title = PlannerStrings.DELETE_CONFIRMATION_TITLE,
            message = PlannerStrings.DELETE_CONFIRMATION_MESSAGE,
            confirmLabel = PlannerStrings.DELETE_CONFIRMATION_CONFIRM,
            dismissLabel = PlannerStrings.DELETE_CONFIRMATION_DISMISS,
            onConfirm = { onEvent(TaskDetailEvent.DeleteConfirmed) },
            onDismiss = { onEvent(TaskDetailEvent.DeleteDismissed) },
        )
    }
}

@Composable
private fun TaskDetailContent(
    uiState: TaskDetailUiState,
    detail: TaskDetail,
    onEvent: (TaskDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val task = detail.task

    LazyColumn(modifier = modifier) {
        item {
            TaskHeader(task = task, onBackClick = { onEvent(TaskDetailEvent.BackClicked) })
        }
        item {
            SectionSpacer {
                TaskInfoCard(task = task)
            }
        }
        item {
            SectionSpacer {
                PrioritySection(priority = task.priority)
            }
        }
        item {
            SectionSpacer {
                TaskCategorySection(category = task.category)
            }
        }
        item {
            SectionSpacer {
                TaskProgressSection(subtasks = detail.subtasks)
            }
        }
        item {
            SectionSpacer {
                SubtaskSection(
                    subtasks = detail.subtasks,
                    onSubtaskToggled = { subtaskId -> onEvent(TaskDetailEvent.SubtaskToggled(subtaskId)) },
                )
            }
        }
        item {
            SectionSpacer {
                AppNotesField(
                    notes = uiState.notesText,
                    onNotesChanged = { text -> onEvent(TaskDetailEvent.NotesChanged(text)) },
                    label = PlannerStrings.TASK_NOTES_LABEL,
                    placeholder = PlannerStrings.TASK_NOTES_PLACEHOLDER,
                )
            }
        }
        item {
            SectionSpacer {
                AttachmentSection(
                    attachments = detail.attachments,
                    onAddAttachmentClick = { onEvent(TaskDetailEvent.AddAttachmentClicked) },
                )
            }
        }
        if (task.source == TaskSource.TRAVEL) {
            item {
                SectionSpacer {
                    RelatedTripSection(onViewTripClick = { onEvent(TaskDetailEvent.ViewRelatedTripClicked) })
                }
            }
        }
        item {
            SectionSpacer {
                RelatedAiSuggestionsSection(
                    onViewSuggestionsClick = { onEvent(TaskDetailEvent.ViewAiSuggestionsClicked) },
                )
            }
        }
        item {
            SectionSpacer {
                TimelineSection(activity = detail.activity)
            }
        }
        item {
            SectionSpacer(bottomPadding = LifeOSSpacing.xl) {
                TaskActionsSection(
                    isCompleted = task.status == TaskStatus.DONE,
                    onCompleteToggleClick = { onEvent(TaskDetailEvent.CompleteToggleClicked) },
                    onDeleteClick = { onEvent(TaskDetailEvent.DeleteClicked) },
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
private fun TaskDetailScreenLoadingPreview() {
    LifeOSTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun TaskDetailScreenErrorPreview() {
    LifeOSTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(isLoading = false, errorMessage = PlannerStrings.DETAIL_LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun TaskDetailScreenEmptyPreview() {
    LifeOSTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(isLoading = false, isNotFound = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** The complete, loaded Task Detail screen — reuses [FakePlannerDataSource] directly. */
@Preview
@Composable
private fun TaskDetailScreenPreview() {
    LifeOSTheme {
        val detail = FakePlannerDataSource().getTaskDetail("task-project-work")
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                isLoading = false,
                detail = detail,
                notesText = detail?.notes.orEmpty(),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** A task whose [TaskSource] is `TRAVEL`, so [RelatedTripSection] renders — [PrioritySection]/etc. still apply. */
@Preview
@Composable
private fun TaskDetailScreenWithRelatedTripPreview() {
    LifeOSTheme {
        val detail = FakePlannerDataSource().getTaskDetail("task-cappadocia-trip")
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                isLoading = false,
                detail = detail,
                notesText = detail?.notes.orEmpty(),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun TaskDetailScreenDeleteConfirmationPreview() {
    LifeOSTheme {
        val detail = FakePlannerDataSource().getTaskDetail("task-project-work")
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                isLoading = false,
                detail = detail,
                notesText = detail?.notes.orEmpty(),
                isDeleteConfirmationVisible = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
