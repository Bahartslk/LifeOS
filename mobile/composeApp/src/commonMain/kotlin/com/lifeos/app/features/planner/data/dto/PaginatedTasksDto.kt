package com.lifeos.app.features.planner.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /planner/tasks`'s full response. Unlike every other Planner endpoint,
 * this list endpoint carries a `meta` block next to `data` (backend's
 * cursor pagination, docs/15-api-design.md#pagination), so it is decoded
 * whole here rather than through `ApiEnvelope`/`dataOrThrow`, which only
 * keep `data`.
 */
@Serializable
data class PaginatedTasksDto(
    val data: List<TaskDto>,
    val meta: PaginationMetaDto,
)

/** [nextCursor] is the last item's id when [hasMore] is `true`, `null` otherwise. */
@Serializable
data class PaginationMetaDto(
    val nextCursor: String? = null,
    val limit: Int,
    val hasMore: Boolean,
)
