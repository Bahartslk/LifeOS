package com.lifeos.app.features.ai.presentation

import com.lifeos.app.features.ai.domain.model.AiTaskContext
import com.lifeos.app.features.ai.domain.model.DailyBrief

/**
 * A load-once, display screen — the same loading/loaded/failed shape every
 * other dashboard-style screen in this app uses. No "not found" state
 * (unlike Task Detail/Trip Detail): there is no single entity to miss,
 * just a context snapshot that either loaded or didn't.
 *
 * [dailyBrief] loads on its own: AI answers are slower than Planner data
 * and can fail independently, so they never block or replace the task
 * context below them.
 */
data class AiAssistantUiState(
    val isLoading: Boolean = true,
    val context: AiTaskContext? = null,
    val errorMessage: String? = null,
    val dailyBrief: DailyBriefUiState = DailyBriefUiState(),
)

data class DailyBriefUiState(
    val isLoading: Boolean = true,
    val brief: DailyBrief? = null,
    val errorMessage: String? = null,
)

sealed interface AiAssistantEvent {
    data object RetryClicked : AiAssistantEvent
    data object ScreenResumed : AiAssistantEvent
    data class TaskClicked(val taskId: String) : AiAssistantEvent
    data object AddTaskClicked : AiAssistantEvent
    data object DailyBriefRefreshClicked : AiAssistantEvent
}

sealed interface AiAssistantAction {
    data class NavigateToTaskDetail(val taskId: String) : AiAssistantAction
    data object NavigateToCreateTask : AiAssistantAction
    data class ShowMessage(val message: String) : AiAssistantAction
}
