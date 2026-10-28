package com.lifeos.app.features.ai.presentation

/**
 * All Turkish UI copy for the AI Assistant's context dashboard, centralized
 * per the pattern established by `HomeStrings`/`TravelStrings`/
 * `PlannerStrings`. This screen has no fake data source of its own to hold
 * data *values* — every value it shows is a real [com.lifeos.app.features.planner.domain.model.Task]
 * from Planner, so unlike those other Strings objects, there is nothing
 * this one defers to a fake-data class for.
 */
internal object AiAssistantStrings {

    // Header — ai-assistent.png "AI Asistan" / "Bugün sana nasıl yardımcı olabilirim?"
    const val HEADER_TITLE = "AI Asistan"
    const val HEADER_SUBTITLE = "Bugün sana nasıl yardımcı olabilirim?"

    // Progress Summary
    const val PROGRESS_TITLE = "İlerleme Özeti"
    fun progressCountLabel(completed: Int, total: Int): String = "$completed / $total"

    // Today's Tasks
    const val TODAY_TASKS_TITLE = "Bugünün Görevleri"
    const val TODAY_TASKS_EMPTY = "Bugün için göreviniz yok."

    // Upcoming Tasks
    const val UPCOMING_TASKS_TITLE = "Yaklaşan Görevler"
    const val UPCOMING_TASKS_EMPTY = "Yaklaşan göreviniz yok."

    // High Priority Tasks
    const val HIGH_PRIORITY_TITLE = "Yüksek Öncelikli Görevler"
    const val HIGH_PRIORITY_EMPTY = "Yüksek öncelikli göreviniz yok."

    // Overdue Tasks
    const val OVERDUE_TITLE = "Gecikmiş Görevler"
    const val OVERDUE_EMPTY = "Gecikmiş göreviniz yok. Harika gidiyorsunuz!"

    // Travel Tasks
    const val TRAVEL_TASKS_TITLE = "Seyahat Görevleri"
    const val TRAVEL_TASKS_EMPTY = "Seyahatle ilgili göreviniz yok."

    // Actions
    const val ADD_TASK_ACTION = "Görev Ekle"

    // Daily Brief (AI)
    const val BRIEF_TITLE = "Günün Özeti"
    const val BRIEF_LOADING = "Günün hazırlanıyor…"
    const val BRIEF_REFRESH_ACTION = "Yenile"
    const val BRIEF_REFRESH_DESCRIPTION = "Günün özetini yenile"
    const val BRIEF_FOCUS_TITLE = "Bugünün odağı"
    const val BRIEF_EVENTS_TITLE = "Öne çıkanlar"
    const val BRIEF_PRIORITIES_TITLE = "Önerilen sıra"
    const val BRIEF_MOTIVATION_TITLE = "Günün motivasyonu"
    const val BRIEF_PARTIAL_MESSAGE = "Özetin bir kısmı alınamadı. Yenilemeyi deneyebilirsin."
    const val BRIEF_ERROR_MESSAGE = "Günün özeti şu anda alınamadı. Lütfen biraz sonra tekrar deneyin."
    const val BRIEF_EMPTY_PRIORITIES = "Bugün için sıralanacak görev yok."

    // Screen states
    const val LOAD_ERROR_MESSAGE = "Yapay zeka bağlamı yüklenemedi. Lütfen tekrar deneyin."
}
