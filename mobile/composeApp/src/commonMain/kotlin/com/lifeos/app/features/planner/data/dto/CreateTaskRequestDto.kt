package com.lifeos.app.features.planner.data.dto

import kotlinx.serialization.Serializable

/**
 * `POST /planner/tasks`'s request body — mirrors backend's `CreateTaskDto`
 * exactly. No `category`/`tags`/`hasReminder`/`estimatedDurationLabel`/`notes`
 * fields: the backend has no columns for any of them (see this iteration's
 * approved mismatch report), so there is nothing to send.
 */
@Serializable
data class CreateTaskRequestDto(
    val title: String,
    val description: String? = null,
    val dueDate: String,
    val dueTime: String? = null,
    val priority: String,
    val taskListId: String? = null,
)
