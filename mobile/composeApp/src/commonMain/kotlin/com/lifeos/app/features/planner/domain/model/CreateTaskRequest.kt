package com.lifeos.app.features.planner.domain.model

/**
 * The raw, user-editable fields "Create Task" collects — deliberately not a
 * second Task-like model (`CreateTaskModel`/`TaskFormModel`): [Task]'s
 * system-assigned fields ([Task.id], [Task.status], [Task.source],
 * [Task.createdAt]) don't exist yet while the form is being filled in, so
 * [com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource.createTask]
 * assigns them once this request reaches it — the same
 * request-in/full-domain-object-out shape [com.lifeos.app.features.travel.domain.model.TripGenerationRequest]
 * already established for Travel's own creation flow.
 *
 * [notes] seeds [com.lifeos.app.features.planner.domain.model.TaskDetail.notes]
 * for this task specifically — Task Detail's own notes *editing* still never
 * persists past the session (see
 * [com.lifeos.app.features.planner.presentation.TaskDetailUiState]'s KDoc);
 * this is a one-time initial value, not a change to that rule.
 */
data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val dueDate: TaskDueDate,
    val priority: TaskPriority,
    val category: TaskCategory,
    val tags: List<String>,
    val hasReminder: Boolean,
    val estimatedDurationLabel: String?,
    val notes: String?,
)
