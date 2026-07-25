package com.lifeos.app.features.ai.domain.model

import com.lifeos.app.features.planner.domain.model.Task

/**
 * The structured snapshot of Planner's task state the AI Assistant reasons
 * over — every category the assistant currently surfaces (Today's Tasks,
 * Upcoming Tasks, High-Priority Tasks, Overdue Tasks, Travel Tasks,
 * Progress Summary), and the same shape a future Gemini/OpenAI-backed use
 * case would receive as prompt context, per this integration's "design so
 * a future AI service can consume one structured context object"
 * requirement.
 *
 * Deliberately a grouping of references to Planner's existing [Task] —
 * never a duplicate task model. The same "aggregate wraps the canonical
 * model, never copies it" shape [com.lifeos.app.features.planner.domain.model.PlannerDashboard]
 * and [com.lifeos.app.features.travel.domain.model.TripDetail] already
 * established; a [Task] can appear in more than one list here (a
 * high-priority task due today appears in both [todayTasks] and
 * [highPriorityTasks]) since these are independent classifications, not a
 * partition.
 */
data class AiTaskContext(
    val todayTasks: List<Task>,
    val upcomingTasks: List<Task>,
    val highPriorityTasks: List<Task>,
    val overdueTasks: List<Task>,
    val travelTasks: List<Task>,
    val progress: AiTaskProgress,
)

/** The completion snapshot behind "Progress Summary" — real counts from Planner, never an AI-invented number. */
data class AiTaskProgress(
    val completedCount: Int,
    val totalCount: Int,
    val completionPercent: Int,
)
