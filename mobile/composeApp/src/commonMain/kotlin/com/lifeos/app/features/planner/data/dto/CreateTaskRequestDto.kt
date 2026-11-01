package com.lifeos.app.features.planner.data.dto

import kotlinx.serialization.Serializable

/**
 * `POST /planner/tasks`'s request body — mirrors backend's `CreateTaskDto`
 * exactly. `category` now has a real backend column and is sent like
 * `priority`, and so are the meeting fields (`type`, `durationMinutes`,
 * `location`, `reminderMinutesBefore`). `tags`/`notes` still have no
 * backend columns, so there is nothing to send for those.
 *
 * `reminderMinutesBefore` is always sent (the shared JSON config encodes
 * defaults), so `null` reaches the backend as "no reminder" rather than as
 * "use the meeting default": the form always decides.
 */
@Serializable
data class CreateTaskRequestDto(
    val title: String,
    val description: String? = null,
    val dueDate: String,
    val dueTime: String? = null,
    val priority: String,
    val category: String,
    val taskListId: String? = null,
    val type: String = "TASK",
    val durationMinutes: Int? = null,
    val location: String? = null,
    val reminderMinutesBefore: Int? = null,
)
