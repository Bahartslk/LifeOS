package com.lifeos.app.features.ai.domain.usecase

import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.ai.domain.model.AiTaskContext
import com.lifeos.app.features.ai.domain.model.AiTaskProgress
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus

/**
 * Builds the AI Assistant's [AiTaskContext] from Planner's already-fetched
 * [PlannerDashboard] — the "AI UseCase" half of this integration's
 * `AiAssistantViewModel -> AI UseCases + Planner UseCases -> merge -> UI`
 * architecture (the same shape the Home and Travel integrations already
 * established). Deliberately takes [PlannerDashboard] as a plain parameter
 * rather than depending on [com.lifeos.app.features.planner.domain.repository.PlannerRepository]
 * itself — no repository-to-repository dependency; fetching stays
 * [com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase]'s
 * job, called separately by [com.lifeos.app.features.ai.presentation.AiAssistantViewModel]
 * and merged there, per this task's explicit architecture requirement.
 *
 * Pure classification over [com.lifeos.app.features.planner.domain.model.Task]s
 * Planner already owns — no AI provider call, no network, nothing
 * persisted. A task can land in more than one output list (a high-priority
 * task due today appears in both [AiTaskContext.todayTasks] and
 * [AiTaskContext.highPriorityTasks]) since these are independent
 * classifications, not a partition.
 */
class BuildAiTaskContextUseCase {

    operator fun invoke(dashboard: PlannerDashboard): AiTaskContext {
        val today = AppToday.date
        val allTasks = dashboard.todayTasks + dashboard.upcomingTasks
        val incompleteTasks = allTasks.filterNot { it.status == TaskStatus.DONE }

        return AiTaskContext(
            todayTasks = dashboard.todayTasks,
            upcomingTasks = dashboard.upcomingTasks,
            highPriorityTasks = incompleteTasks.filter { it.priority == TaskPriority.HIGH },
            overdueTasks = incompleteTasks.filter { it.dueDate.date < today },
            travelTasks = allTasks.filter { it.source == TaskSource.TRAVEL },
            progress = AiTaskProgress(
                completedCount = dashboard.overview.completedTaskCount,
                totalCount = dashboard.overview.totalTaskCount,
                completionPercent = dashboard.overview.productivityPercent,
            ),
        )
    }
}
