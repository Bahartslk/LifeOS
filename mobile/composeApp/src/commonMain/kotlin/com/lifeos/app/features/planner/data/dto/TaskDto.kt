package com.lifeos.app.features.planner.data.dto

import kotlinx.serialization.Serializable

/**
 * The wire shape of `TaskResponseDto` (backend's `planner/dto/task-response.dto.ts`).
 * `priority`/`status`/`source` stay plain strings here rather than reusing
 * `domain.model.TaskPriority`/`TaskStatus`/`TaskSource` directly — those enums
 * have no `kotlinx.serialization` annotation (domain must not depend on a
 * serialization library) — `features/planner/data/mapper/PlannerMappers.kt`
 * converts each via `valueOf`, safe because every value is byte-for-byte
 * identical between mobile and backend (see Prisma schema's own "Mirrors
 * TaskStatus on mobile exactly" comment).
 */
@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val dueDate: String,
    val dueTime: String? = null,
    val priority: String,
    val status: String,
    val source: String,
    val taskListId: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
