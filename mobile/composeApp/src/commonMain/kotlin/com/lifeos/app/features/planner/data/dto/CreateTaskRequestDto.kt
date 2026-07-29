package com.lifeos.app.features.planner.data.dto

import kotlinx.serialization.Serializable

/**
 * `POST /planner/tasks`'s request body — mirrors backend's `CreateTaskDto`
 * exactly. `category` now has a real backend column and is sent like
 * `priority`; `tags`/`hasReminder`/`estimatedDurationLabel`/`notes` still
 * have no backend columns (see this iteration's approved mismatch report),
 * so there is nothing to send for those.
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
)
