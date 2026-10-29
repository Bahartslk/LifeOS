package com.lifeos.app.features.ai.domain.usecase

import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.ai.domain.model.DailyReminder
import com.lifeos.app.features.ai.domain.model.ReminderRelatedTo
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.usecase.CreateTaskUseCase
import kotlinx.datetime.LocalDate

/**
 * Turns an AI reminder suggestion into a real Planner task through Planner's
 * own [CreateTaskUseCase]; the AI never creates anything by itself.
 *
 * - Due date: the suggested date, or today when there is none or it has
 *   already passed (a reminder in the past is of no use).
 * - Category: [TaskCategory.TRAVEL] for travel-related suggestions,
 *   otherwise [TaskCategory.PERSONAL]. Priority: [TaskPriority.MEDIUM].
 * - The AI's reason becomes the task description. The request is marked
 *   `hasReminder`, but the backend doesn't store that flag yet (no reminder
 *   or notification exists until push notifications are added).
 */
class AddReminderToAgendaUseCase(
    private val createTask: CreateTaskUseCase,
    private val today: () -> LocalDate = { AppToday.date },
) {
    suspend operator fun invoke(reminder: DailyReminder): Result<Task> = createTask(reminder.toCreateTaskRequest())

    private fun DailyReminder.toCreateTaskRequest(): CreateTaskRequest {
        val todayDate = today()
        val dueDate = recommendedDate?.takeIf { it >= todayDate } ?: todayDate
        return CreateTaskRequest(
            title = title,
            description = reason.ifBlank { null },
            dueDate = TaskDueDate(date = dueDate),
            priority = TaskPriority.MEDIUM,
            category = if (relatedTo == ReminderRelatedTo.PLANNER) TaskCategory.PERSONAL else TaskCategory.TRAVEL,
            tags = emptyList(),
            hasReminder = true,
            estimatedDurationLabel = null,
            notes = null,
        )
    }
}
