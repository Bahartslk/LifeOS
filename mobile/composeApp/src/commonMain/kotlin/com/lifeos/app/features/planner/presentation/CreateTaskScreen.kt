package com.lifeos.app.features.planner.presentation

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
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.presentation.sections.CategorySelector
import com.lifeos.app.features.planner.presentation.sections.DescriptionField
import com.lifeos.app.features.planner.presentation.sections.DueDateField
import com.lifeos.app.features.planner.presentation.sections.EstimatedDurationField
import com.lifeos.app.features.planner.presentation.sections.PrioritySelector
import com.lifeos.app.features.planner.presentation.sections.ReminderSection
import com.lifeos.app.features.planner.presentation.sections.RepeatSection
import com.lifeos.app.features.planner.presentation.sections.SaveActionsSection
import com.lifeos.app.features.planner.presentation.sections.TagsSection
import com.lifeos.app.features.planner.presentation.sections.TaskTitleField
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [CreateTaskViewModel] and forwards its one-shot
 * [CreateTaskAction]s to navigation or a snackbar, per the same Route/Screen
 * split established by every other feature.
 */
@Composable
fun CreateTaskRoute(
    onNavigateBack: () -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            CreateTaskAction.NavigateBack -> onNavigateBack()
            is CreateTaskAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    CreateTaskScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/**
 * The stateless, previewable screen. Composed entirely from section
 * composables (per this feature's "no single huge screen" rule), the same
 * "form is always the natural state, failures surface as a snackbar"
 * approach [com.lifeos.app.features.travel.presentation.CreateTripRoute]'s
 * screen already established — there is no separate loading/empty/error
 * full-screen state here either.
 */
@Composable
private fun CreateTaskScreen(
    uiState: CreateTaskUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CreateTaskEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = PlannerStrings.CREATE_TASK_TITLE,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = PlannerStrings.BACK_CONTENT_DESCRIPTION,
                onNavigationClick = { onEvent(CreateTaskEvent.BackClicked) },
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(LifeOSSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
        ) {
            item {
                Text(text = PlannerStrings.CREATE_TASK_SUBTITLE, style = MaterialTheme.typography.bodyMedium)
            }
            item {
                TaskTitleField(
                    title = uiState.title,
                    onTitleChanged = { onEvent(CreateTaskEvent.TitleChanged(it)) },
                    errorMessage = uiState.titleError,
                )
            }
            item {
                DescriptionField(
                    description = uiState.description,
                    onDescriptionChanged = { onEvent(CreateTaskEvent.DescriptionChanged(it)) },
                )
            }
            item {
                DueDateField(
                    dueDate = uiState.dueDate,
                    onDueDateChanged = { onEvent(CreateTaskEvent.DueDateChanged(it)) },
                    errorMessage = uiState.dueDateError,
                )
            }
            item {
                ReminderSection(
                    hasReminder = uiState.hasReminder,
                    onReminderToggled = { onEvent(CreateTaskEvent.ReminderToggled(it)) },
                )
            }
            item {
                PrioritySelector(
                    selectedPriority = uiState.priority,
                    onPrioritySelected = { onEvent(CreateTaskEvent.PrioritySelected(it)) },
                )
            }
            item {
                CategorySelector(
                    selectedCategory = uiState.category,
                    onCategorySelected = { onEvent(CreateTaskEvent.CategorySelected(it)) },
                )
            }
            item {
                TagsSection(
                    tags = uiState.tags,
                    tagInput = uiState.tagInput,
                    onTagInputChanged = { onEvent(CreateTaskEvent.TagInputChanged(it)) },
                    onTagAdded = { onEvent(CreateTaskEvent.TagAdded) },
                    onTagRemoved = { onEvent(CreateTaskEvent.TagRemoved(it)) },
                )
            }
            item {
                EstimatedDurationField(
                    estimatedDuration = uiState.estimatedDuration,
                    onEstimatedDurationChanged = { onEvent(CreateTaskEvent.EstimatedDurationChanged(it)) },
                )
            }
            item {
                RepeatSection(
                    selectedOption = uiState.repeatOption,
                    onOptionSelected = { onEvent(CreateTaskEvent.RepeatOptionSelected(it)) },
                )
            }
            item {
                AppNotesField(
                    notes = uiState.notes,
                    onNotesChanged = { onEvent(CreateTaskEvent.NotesChanged(it)) },
                    label = PlannerStrings.TASK_NOTES_LABEL,
                    placeholder = PlannerStrings.TASK_NOTES_PLACEHOLDER,
                )
            }
            item {
                SaveActionsSection(
                    isSaving = uiState.isSaving,
                    onSaveClick = { onEvent(CreateTaskEvent.SaveClicked) },
                    onCancelClick = { onEvent(CreateTaskEvent.BackClicked) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun CreateTaskScreenEmptyPreview() {
    LifeOSTheme {
        CreateTaskScreen(
            uiState = CreateTaskUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun CreateTaskScreenValidationErrorPreview() {
    LifeOSTheme {
        CreateTaskScreen(
            uiState = CreateTaskUiState(
                titleError = PlannerStrings.TITLE_REQUIRED_ERROR,
                dueDateError = PlannerStrings.DUE_DATE_REQUIRED_ERROR,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun CreateTaskScreenFilledPreview() {
    LifeOSTheme {
        CreateTaskScreen(
            uiState = CreateTaskUiState(
                title = "Sunum hazırlığı",
                description = "Yönetim kurulu için slaytları tamamla",
                dueDate = "Yarın, 14:00",
                hasReminder = true,
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK,
                tags = listOf("yönetim", "sunum"),
                estimatedDuration = "45 dakika",
                repeatOption = RepeatOption.WEEKLY,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun CreateTaskScreenSavingPreview() {
    LifeOSTheme {
        CreateTaskScreen(
            uiState = CreateTaskUiState(title = "Sunum hazırlığı", dueDate = "Yarın, 14:00", isSaving = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
