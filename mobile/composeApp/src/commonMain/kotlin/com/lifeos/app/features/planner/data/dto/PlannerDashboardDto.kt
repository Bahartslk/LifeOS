package com.lifeos.app.features.planner.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /planner/dashboard`'s response — only the fields mobile's
 * [com.lifeos.app.features.planner.domain.model.PlannerDashboard] actually
 * uses. Backend's `PlannerDashboardResponseDto` also has `highPriorityTasks`/
 * `travelTasks`; omitted here since nothing on mobile consumes them yet and
 * `HttpClientFactory.json`'s `ignoreUnknownKeys = true` means leaving them
 * off this DTO is safe, not a partial/lossy decode.
 */
@Serializable
data class PlannerDashboardDto(
    val todayTasks: List<TaskDto>,
    val upcomingTasks: List<TaskDto>,
    val completedCount: Int,
    val pendingCount: Int,
    val progressPercentage: Int,
)
