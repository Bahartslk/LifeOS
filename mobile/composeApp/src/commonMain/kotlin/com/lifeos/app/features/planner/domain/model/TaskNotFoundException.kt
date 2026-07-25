package com.lifeos.app.features.planner.domain.model

/**
 * Thrown by [com.lifeos.app.features.planner.domain.repository.PlannerRepository.getTaskDetail]
 * when no task matches the given id. A domain-level type (not a data-layer
 * exception reused across layers) so [com.lifeos.app.features.planner.presentation.TaskDetailViewModel]
 * can distinguish "task not found" (an Empty State) from any other failure
 * (an Error State) without depending on anything in `data` — the same
 * pattern [com.lifeos.app.features.travel.domain.model.TripNotFoundException]
 * already established.
 */
class TaskNotFoundException(taskId: String) : Exception("Task not found: $taskId")
