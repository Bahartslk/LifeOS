package com.lifeos.app.features.planner.presentation

/**
 * All Turkish UI copy for the Planner feature, centralized per the pattern
 * established by `AuthStrings`/`HomeStrings`/`TravelStrings`. Fake *data
 * values* (a task's actual title, due date) live in
 * [FakePlannerDataSource][com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource]
 * instead.
 */
internal object PlannerStrings {

    // Header — planner.png "Ajandam"
    const val BRAND_NAME = "LifeOS"
    const val TITLE = "Ajandam"
    const val SUBTITLE = "Bugünkü planın yapay zeka tarafından optimize edildi."
    const val SETTINGS_CONTENT_DESCRIPTION = "Ayarlar"

    // Today's Overview
    const val AI_PRODUCTIVITY_BADGE = "AI Verimlilik"
    const val STAT_TOTAL_TASKS = "TOPLAM GÖREV"
    const val STAT_UPCOMING_EVENTS = "YAKLAŞAN ETKİNLİK"
    const val STAT_COMPLETED = "TAMAMLANAN"
    const val STAT_PRODUCTIVITY = "VERİMLİLİK %"

    // Today's Productivity (TaskProgressIndicator)
    const val PROGRESS_LABEL = "Bugünkü İlerleme"
    fun progressCountLabel(completed: Int, total: Int): String = "$completed / $total"

    // Progress Section (AI insight card)
    const val UPDATE_PLAN_ACTION = "Planı Güncelle"
    const val UPDATE_PLAN_COMING_SOON = "Plan güncelleme özelliği yakında kullanıma sunulacak."

    // Calendar Section
    const val CALENDAR_PREVIOUS_MONTH_DESCRIPTION = "Önceki ay"
    const val CALENDAR_NEXT_MONTH_DESCRIPTION = "Sonraki ay"

    // Today's Tasks — "Günlük Akış"
    const val TODAY_TASKS_TITLE = "Günlük Akış"
    const val TODAY_TASKS_EMPTY_TITLE = "Bugün için göreviniz yok"
    const val TODAY_TASKS_EMPTY_DESCRIPTION = "Bugünü boş geçirin veya yeni bir görev ekleyin."
    const val STATUS_COMPLETED_LABEL = "Tamamlandı"
    const val PRIORITY_HIGH_LABEL = "Önemli"
    fun tagCountLabel(count: Int): String = "+$count"
    const val TASK_TOGGLE_CONTENT_DESCRIPTION = "Görev durumunu değiştir"
    const val DUE_DATE_NOW_SUFFIX = "Şimdi"

    // Overdue Tasks — unfinished tasks due before today (the dashboard's overdueTasks).
    const val OVERDUE_TASKS_TITLE = "Gecikmiş Görevler"
    const val OVERDUE_LABEL = "Gecikti"

    fun overdueDueDateLabel(dateLabel: String): String = "$OVERDUE_LABEL · $dateLabel"

    // Upcoming Tasks — "Yaklaşan Önemli Tarihler"
    const val UPCOMING_TASKS_TITLE = "Yaklaşan Önemli Tarihler"
    const val UPCOMING_TASKS_EMPTY_TITLE = "Yaklaşan önemli bir tarih yok"
    const val UPCOMING_TASKS_EMPTY_DESCRIPTION = "Yeni bir görev veya seyahat eklediğinizde burada görünecek."

    // Quick Categories
    const val CATEGORIES_TITLE = "Hızlı Kategoriler"
    const val CATEGORY_ALL = "Tümü"
    const val CATEGORY_WORK = "İş"
    const val CATEGORY_PERSONAL = "Kişisel"
    const val CATEGORY_HEALTH = "Sağlık"
    const val CATEGORY_TRAVEL = "Seyahat"
    const val CATEGORY_FINANCE = "Finans"

    // Quick Actions
    const val QUICK_ACTIONS_TITLE = "Hızlı İşlemler"
    const val QUICK_ACTION_NEW_TASK = "Yeni Görev"
    const val QUICK_ACTION_VIEW_CALENDAR = "Takvim"
    const val QUICK_ACTION_REMINDERS = "Hatırlatıcılar"
    const val QUICK_ACTION_VIEW_REPORT = "Raporu Görüntüle"
    const val REMINDERS_COMING_SOON = "Hatırlatıcılar yakında kullanıma sunulacak."
    const val REPORT_COMING_SOON = "Rapor görüntüleme yakında kullanıma sunulacak."

    // FAB
    const val FAB_NEW_TASK = "Yeni Görev"

    // Screen states
    const val LOAD_ERROR_MESSAGE = "Ajanda yüklenemedi. Lütfen tekrar deneyin."
    const val EMPTY_TITLE = "Henüz göreviniz yok"
    const val EMPTY_DESCRIPTION = "İlk görevinizi oluşturarak ajandanızı planlamaya başlayın."

    // --- Task Detail ---

    const val HEADER_TITLE = "Görev Detayı"
    const val BACK_CONTENT_DESCRIPTION = "Geri"

    // Task Info Card
    const val INFO_TITLE = "Görev Bilgileri"
    const val DESCRIPTION_LABEL = "Açıklama"
    const val DESCRIPTION_EMPTY = "Açıklama eklenmedi."
    const val DUE_DATE_LABEL = "Son Tarih"
    const val REMINDER_LABEL = "Hatırlatıcı"
    const val REMINDER_ON = "Açık"
    const val REMINDER_OFF = "Kapalı"
    const val STATUS_LABEL = "Durum"
    const val TAGS_LABEL = "Etiketler"
    const val TASK_STATUS_TODO_LABEL = "Yapılacak"
    const val TASK_STATUS_IN_PROGRESS_LABEL = "Devam Ediyor"

    // Priority Section
    const val PRIORITY_TITLE = "Öncelik"
    const val TASK_PRIORITY_LOW_LABEL = "Düşük"
    const val TASK_PRIORITY_MEDIUM_LABEL = "Orta"
    const val TASK_PRIORITY_HIGH_LABEL = "Yüksek"

    // Task Category Section
    const val TASK_CATEGORY_TITLE = "Kategori"

    // Subtask Section
    const val SUBTASKS_TITLE = "Alt Görevler"
    const val SUBTASKS_EMPTY = "Bu görev için alt görev eklenmedi."
    const val SUBTASK_TOGGLE_CONTENT_DESCRIPTION = "Alt görev durumunu değiştir"

    // Task Progress Section
    const val TASK_PROGRESS_TITLE = "İlerleme"

    // Notes Section
    const val TASK_NOTES_LABEL = "Notlar"
    const val TASK_NOTES_PLACEHOLDER = "Bu görevle ilgili notlarınızı buraya ekleyin..."

    // Attachment Section
    const val ATTACHMENTS_TITLE = "Ekler"
    const val ATTACHMENTS_EMPTY = "Henüz ek eklenmedi."
    const val ADD_ATTACHMENT_ACTION = "Ek Ekle"
    const val ATTACHMENT_COMING_SOON = "Ek ekleme özelliği yakında kullanıma sunulacak."

    // Related Trip Section (placeholder)
    const val RELATED_TRIP_TITLE = "İlgili Seyahat"
    const val RELATED_TRIP_DESCRIPTION = "Bu görev bir seyahatle ilişkilendirildi."
    const val RELATED_TRIP_ACTION = "Seyahati Görüntüle"
    const val RELATED_TRIP_COMING_SOON = "Bu görev için seyahat görüntüleme yakında kullanıma sunulacak."

    // Related AI Suggestions Section (placeholder)
    const val AI_SUGGESTIONS_TITLE = "AI Önerileri"
    const val AI_SUGGESTIONS_DESCRIPTION = "Yapay zeka bu görev için öneriler sunabilir."
    const val AI_SUGGESTIONS_ACTION = "Önerileri Gör"
    const val AI_SUGGESTIONS_COMING_SOON = "AI önerileri yakında kullanıma sunulacak."

    // Timeline Section (Activity)
    const val ACTIVITY_TITLE = "Etkinlik Geçmişi"

    // Task Actions Section
    const val ACTION_MARK_COMPLETE = "Tamamlandı Olarak İşaretle"
    const val ACTION_MARK_INCOMPLETE = "Tamamlanmadı Olarak İşaretle"
    const val ACTION_DELETE = "Görevi Sil"
    const val DELETE_CONFIRMATION_TITLE = "Görevi Sil"
    const val DELETE_CONFIRMATION_MESSAGE = "Bu görevi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz."
    const val DELETE_CONFIRMATION_CONFIRM = "Sil"
    const val DELETE_CONFIRMATION_DISMISS = "Vazgeç"

    // Screen states
    const val DETAIL_LOAD_ERROR_MESSAGE = "Görev yüklenemedi. Lütfen tekrar deneyin."
    const val DETAIL_EMPTY_TITLE = "Görev bulunamadı"
    const val DETAIL_EMPTY_DESCRIPTION = "Bu görev kaldırılmış veya artık mevcut değil."

    // --- Create Task ---

    const val CREATE_TASK_TITLE = "Yeni Görev"
    const val CREATE_TASK_SUBTITLE = "Yeni görevinizin ayrıntılarını girin."

    // Task Title Field
    const val TITLE_FIELD_LABEL = "Görev Başlığı"
    const val TITLE_FIELD_PLACEHOLDER = "Örn. Sunum hazırlığı"
    const val TITLE_REQUIRED_ERROR = "Görev başlığı zorunludur."

    // Description Field
    const val DESCRIPTION_FIELD_PLACEHOLDER = "Görev hakkında kısa bir açıklama ekleyin..."

    // Due Date Field
    const val DUE_DATE_FIELD_PLACEHOLDER = "Örn. Yarın, 14:00"
    const val DUE_DATE_REQUIRED_ERROR = "Son tarih zorunludur."
    const val DUE_DATE_INVALID_FORMAT_ERROR = "Tarih anlaşılamadı. Örn: \"Yarın, 14:00\" veya \"12 Kasım\"."

    // Reminder Section
    const val REMINDER_TOGGLE_SUBTITLE = "Son tarihte bildirim al"
    const val REMINDER_TOGGLE_CONTENT_DESCRIPTION = "Hatırlatıcıyı aç/kapat"

    // Estimated Duration Field
    const val ESTIMATED_DURATION_LABEL = "Tahmini Süre"
    const val ESTIMATED_DURATION_PLACEHOLDER = "Örn. 45 dakika"

    // Repeat Section (UI only)
    const val REPEAT_TITLE = "Tekrarlama"
    const val REPEAT_NONE_LABEL = "Yok"
    const val REPEAT_DAILY_LABEL = "Günlük"
    const val REPEAT_WEEKLY_LABEL = "Haftalık"
    const val REPEAT_MONTHLY_LABEL = "Aylık"

    // Tags Section (Create Task's editable variant)
    const val TAGS_INPUT_PLACEHOLDER = "Etiket ekle..."
    const val TAGS_ADD_CONTENT_DESCRIPTION = "Etiket ekle"
    const val TAGS_REMOVE_CONTENT_DESCRIPTION = "Etiketi kaldır"

    // Save Actions Section
    const val SAVE_ACTION = "Kaydet"
    const val CANCEL_ACTION = "İptal"
    const val CREATE_TASK_SAVE_ERROR_MESSAGE = "Görev kaydedilemedi. Lütfen tekrar deneyin."

    // --- Planner Calendar ---

    const val CALENDAR_HEADER_TITLE = "Takvim"
    const val CALENDAR_LOAD_ERROR_MESSAGE = "Takvim yüklenemedi. Lütfen tekrar deneyin."

    // Calendar Grid / Day cells
    fun calendarDayContentDescription(day: Int, taskCount: Int, isToday: Boolean): String = buildString {
        append(day)
        if (isToday) append(", $CALENDAR_TODAY_LABEL")
        if (taskCount > 0) append(", ${calendarTaskCountLabel(taskCount)}")
    }
    const val CALENDAR_TODAY_LABEL = "bugün"
    fun calendarTaskCountLabel(count: Int): String = if (count == 1) "1 görev" else "$count görev"

    // Selected Day Agenda
    const val CALENDAR_AGENDA_EMPTY_TITLE = "Bu gün için göreviniz yok"
    const val CALENDAR_AGENDA_EMPTY_DESCRIPTION = "Farklı bir gün seçin veya yeni bir görev oluşturun."
}
