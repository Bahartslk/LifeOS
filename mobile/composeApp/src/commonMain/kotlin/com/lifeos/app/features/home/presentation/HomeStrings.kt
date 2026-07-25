package com.lifeos.app.features.home.presentation

/**
 * All Turkish UI copy for the Home Dashboard feature, centralized per the
 * pattern established by `features/auth/presentation/AuthStrings.kt` and
 * `core/designsystem/DesignSystemStrings.kt`. Fake *data values* (a task's
 * actual title, a trip's destination name) live in [FakeHomeDataSource][
 * com.lifeos.app.features.home.data.datasource.FakeHomeDataSource] instead —
 * this object holds only fixed screen chrome that exists regardless of what
 * data is loaded.
 */
internal object HomeStrings {

    // Greeting — home.png "Good morning, Bahar 👋"
    const val GREETING_MORNING = "Günaydın"
    const val GREETING_AFTERNOON = "İyi günler"
    const val GREETING_EVENING = "İyi akşamlar"
    const val GREETING_SUBTITLE = "Bugünkü özetiniz yapay zeka tarafından hazırlandı"
    const val SETTINGS_CONTENT_DESCRIPTION = "Ayarlar"
    const val AVATAR_CONTENT_DESCRIPTION = "Profil fotoğrafı"

    // Intelligent Hub — home.png "Your day at a glance."
    const val HUB_EYEBROW = "AKILLI MERKEZ"
    const val HUB_TITLE = "Gününüze genel bakış."
    const val HUB_COMPLETE_CHECKLIST = "Kontrol Listesini Tamamla"
    const val HUB_VIEW_TRIP = "Seyahati Görüntüle"
    // Built from real Planner/Travel data (Iteration 4) — no PACKING/WEATHER
    // equivalent exists since neither backend has that data; see
    // HomeViewModel's highlight-building KDoc.
    fun hubTripHighlight(daysUntilStart: Int): String = "Seyahatiniz $daysUntilStart gün sonra başlıyor"
    fun hubTaskHighlight(count: Int): String = "Bugün için $count önemli göreviniz var"

    // Quick Actions — home.png "New Task / Create Trip / Ask AI / Notes"
    const val QUICK_ACTION_NEW_TASK = "Yeni Görev"
    const val QUICK_ACTION_CREATE_TRIP = "Seyahat Oluştur"
    const val QUICK_ACTION_ASK_AI = "AI Asistanına Sor"
    const val QUICK_ACTION_CREATE_NOTE = "Not Oluştur"
    const val QUICK_ACTION_NOTES_COMING_SOON = "Not özelliği yakında kullanıma sunulacak."

    // Overview — home.png "Overview" / "TASKS COMPLETED" / "PRODUCTIVITY SCORE"
    const val OVERVIEW_TITLE = "Genel Bakış"
    const val OVERVIEW_TASKS_COMPLETED_LABEL = "TAMAMLANAN GÖREVLER"
    const val OVERVIEW_PRODUCTIVITY_LABEL = "VERİMLİLİK PUANI"
    const val OVERVIEW_VIEW_INSIGHTS = "Detaylı İçgörüleri Görüntüle"
    fun overviewProductivitySummary(deltaPercent: Int): String =
        "Verimliliğiniz geçen haftaya göre %$deltaPercent daha yüksek. Harika iş!"

    // Today's Priorities — home.png "Today's Priority" / "Manage All"
    const val PRIORITIES_TITLE = "Bugünün Öncelikleri"
    const val PRIORITIES_MANAGE_ALL = "Tümünü Yönet"
    const val PRIORITIES_EMPTY_TITLE = "Bugün için göreviniz yok"
    const val PRIORITIES_EMPTY_DESCRIPTION = "Harika gidiyor! Yeni bir görev eklemek ister misiniz?"
    const val PRIORITY_HIGH_LABEL = "YÜKSEK ÖNCELİK"

    // Today's Priorities — Task category labels (Planner integration; StatusChip expects uppercase Turkish)
    const val CATEGORY_WORK = "İŞ"
    const val CATEGORY_PERSONAL = "KİŞİSEL"
    const val CATEGORY_HEALTH = "SAĞLIK"
    const val CATEGORY_TRAVEL = "SEYAHAT"
    const val CATEGORY_FINANCE = "FİNANS"

    // Upcoming Journey — home.png "Your Next Adventure"
    const val JOURNEY_TITLE = "Sıradaki Maceranız"
    const val JOURNEY_FLIGHT_LABEL = "UÇUŞ"
    const val JOURNEY_STAY_LABEL = "KONAKLAMA"
    const val JOURNEY_VIEW_MAP = "Yolculuk Haritasını Görüntüle"
    const val JOURNEY_PACKING_LIST = "Paketleme Listesi"
    const val JOURNEY_EMPTY_TITLE = "Henüz planlanmış bir seyahatiniz yok"
    const val JOURNEY_EMPTY_DESCRIPTION = "Yapay zeka destekli seyahat planlayıcısıyla ilk seyahatinizi oluşturun."
    fun journeyDaysRemainingLabel(days: Int): String = "$days GÜN KALDI"
    fun journeyWeatherLabel(celsius: Int): String = "$celsius°"

    // Screen-level states
    const val LOAD_ERROR_MESSAGE = "Kontrol paneli yüklenemedi. Lütfen tekrar deneyin."
}
