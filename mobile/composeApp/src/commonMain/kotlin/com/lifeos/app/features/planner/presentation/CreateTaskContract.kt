package com.lifeos.app.features.planner.presentation

import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskType
import kotlinx.datetime.LocalTime

/**
 * "Repeat Option" (Create Task's requirement — explicitly UI only). Lives
 * here, in the presentation layer, rather than `domain/model` — it is never
 * sent to [com.lifeos.app.features.planner.domain.model.CreateTaskRequest]
 * or stored anywhere, so treating it as a domain concept would misrepresent
 * what this app actually persists.
 */
enum class RepeatOption { NONE, DAILY, WEEKLY, MONTHLY }

/**
 * All form fields default to an empty/neutral "nothing chosen yet" state,
 * the same convention [com.lifeos.app.features.travel.presentation.CreateTripUiState]
 * established — except [priority]/[category], which (like
 * [com.lifeos.app.features.travel.presentation.CreateTripUiState.travelStyle])
 * always need exactly one [OptionChipRow][com.lifeos.app.core.designsystem.components.OptionChipRow]
 * selection, so each defaults to its most common enum entry rather than
 * being nullable.
 *
 * [titleError]/[dueDateError] are field-level validation messages —
 * surfaced inline by [com.lifeos.app.features.planner.presentation.sections.TaskTitleField]/
 * [com.lifeos.app.features.planner.presentation.sections.DueDateField]
 * themselves, not a screen-replacing [com.lifeos.app.core.designsystem.components.ErrorView].
 * There is no `errorMessage` field for the same reason
 * [com.lifeos.app.features.travel.presentation.CreateTripUiState] has none:
 * every failure here (validation, save) must leave the user's already-filled
 * form intact, so save failures surface as a one-shot
 * [CreateTaskAction.ShowMessage] snackbar instead.
 */
data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val type: TaskType = TaskType.TASK,
    /** Picked with the time picker; when null, a time typed into [dueDate] ("Yarın, 14:00") is used. */
    val time: LocalTime? = null,
    val isTimePickerVisible: Boolean = false,
    val durationMinutes: Int? = null,
    val location: String = "",
    /** Null = no reminder. Becomes 10 when the type switches to a meeting with no reminder chosen. */
    val reminderMinutesBefore: Int? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val repeatOption: RepeatOption = RepeatOption.NONE,
    val notes: String = "",
    val titleError: String? = null,
    val dueDateError: String? = null,
    val timeError: String? = null,
    val isSaving: Boolean = false,
)

/** The duration choices on the form, in minutes (null = not set). */
val DURATION_OPTIONS: List<Int?> = listOf(null, 15, 30, 45, 60, 90, 120)

/** The reminder choices on the form, in minutes before (null = none, 0 = at the time). */
val REMINDER_OPTIONS: List<Int?> = listOf(null, 0, 5, 10, 15, 30, 60)

/** A new meeting's reminder (product decision; the backend uses the same default). */
const val DEFAULT_MEETING_REMINDER_MINUTES = 10

sealed interface CreateTaskEvent {
    data object BackClicked : CreateTaskEvent
    data class TitleChanged(val value: String) : CreateTaskEvent
    data class DescriptionChanged(val value: String) : CreateTaskEvent
    data class DueDateChanged(val value: String) : CreateTaskEvent
    data class TypeSelected(val type: TaskType) : CreateTaskEvent
    data object TimeFieldClicked : CreateTaskEvent
    data class TimeSelected(val time: LocalTime) : CreateTaskEvent
    data object TimeCleared : CreateTaskEvent
    data object TimePickerDismissed : CreateTaskEvent
    data class DurationSelected(val minutes: Int?) : CreateTaskEvent
    data class LocationChanged(val value: String) : CreateTaskEvent
    data class ReminderSelected(val minutes: Int?) : CreateTaskEvent
    data class PrioritySelected(val priority: TaskPriority) : CreateTaskEvent
    data class CategorySelected(val category: TaskCategory) : CreateTaskEvent
    data class TagInputChanged(val value: String) : CreateTaskEvent
    data object TagAdded : CreateTaskEvent
    data class TagRemoved(val tag: String) : CreateTaskEvent
    data class RepeatOptionSelected(val option: RepeatOption) : CreateTaskEvent
    data class NotesChanged(val value: String) : CreateTaskEvent
    data object SaveClicked : CreateTaskEvent
}

sealed interface CreateTaskAction {
    data object NavigateBack : CreateTaskAction
    data class ShowMessage(val message: String) : CreateTaskAction
}
