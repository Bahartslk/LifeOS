package com.lifeos.app.features.travel.presentation

/**
 * All Turkish UI copy for the Travel feature — both Travel List and Travel
 * Detail — centralized per the pattern established by
 * `AuthStrings`/`HomeStrings`. Fake *data values* (a trip's actual
 * destination, dates) live in
 * [FakeTravelDataSource][com.lifeos.app.features.travel.data.datasource.FakeTravelDataSource]
 * instead.
 */
internal object TravelStrings {

    // Header — travel-list.png "World Journeys"
    const val TITLE = "Dünya Yolculukları"
    const val SUBTITLE = "Küratörlü deneyimler ve gelecek anılar."
    const val SEARCH_ACTION_DESCRIPTION = "Seyahatlerde ara"
    const val FILTER_ACTION_DESCRIPTION = "Seyahatleri filtrele"

    // Search
    const val SEARCH_PLACEHOLDER = "Seyahat veya şehir ara"

    // Filters
    const val FILTER_ALL = "Tümü"
    const val FILTER_UPCOMING = "Yaklaşan"
    const val FILTER_PAST = "Geçmiş"

    // Quick Statistics
    const val STAT_TOTAL_TRIPS = "TOPLAM SEYAHAT"
    const val STAT_COUNTRIES_VISITED = "ZİYARET EDİLEN ÜLKE"
    const val STAT_UPCOMING_TRIPS = "YAKLAŞAN SEYAHAT"

    // Upcoming Journeys — no visible section title, per travel-list.png
    const val AI_OPTIMIZED_BADGE = "AI Optimize Plan"
    fun daysUntilLabel(days: Int): String = "$days GÜN SONRA"
    fun weatherLabel(celsius: Int): String = "$celsius°"
    const val STATUS_CONFIRMED = "Onaylandı"

    // Past Journeys
    const val PAST_SECTION_TITLE = "Anılar"
    const val COMPLETED_BADGE = "TAMAMLANDI"
    fun photoCountLabel(count: Int): String = "$count Fotoğraf"
    const val TRIP_OPTIONS_DESCRIPTION = "Seyahat seçenekleri"
    const val TRIP_OPTIONS_COMING_SOON = "Seyahat seçenekleri yakında kullanıma sunulacak."

    // Past Archives teaser card
    const val ARCHIVE_TITLE = "Geçmiş Arşiv"
    const val ARCHIVE_VIEW_ACTION = "Görüntüle"
    fun archiveDescription(count: Int, yearRange: String): String =
        "$yearRange yıllarından $count eski yolculuğu görüntüleyin"

    // FAB
    const val FAB_NEW_TRIP = "Yeni Seyahat"

    // Screen states
    const val LOAD_ERROR_MESSAGE = "Seyahatler yüklenemedi. Lütfen tekrar deneyin."
    const val EMPTY_TITLE = "Henüz seyahatiniz yok"
    const val EMPTY_DESCRIPTION = "İlk seyahatinizi oluşturarak yolculuğunuza başlayın."
    const val TRIP_DETAIL_COMING_SOON = "Seyahat detayı yakında kullanıma sunulacak."

    // --- Travel Detail (travel-details.png) ---

    // Hero Section
    const val BACK_CONTENT_DESCRIPTION = "Geri"
    const val SHARE_CONTENT_DESCRIPTION = "Paylaş"
    const val SHARE_COMING_SOON = "Paylaşma özelliği yakında kullanıma sunulacak."

    // AI Summary Card
    const val AI_SUMMARY_TITLE = "AI Tahmini"
    fun aiWeatherLabel(celsius: Int): String = "Hava: $celsius°C"
    fun aiWindLabel(kmh: Int): String = "Rüzgar: $kmh km/s"

    // Timeline Section
    const val TIMELINE_TITLE = "Günlük Program"
    const val PERIOD_MORNING = "Sabah"
    const val PERIOD_AFTERNOON = "Öğleden Sonra"
    const val PERIOD_EVENING = "Akşam"
    const val EXPAND_DAY_DESCRIPTION = "Günü genişlet"
    const val COLLAPSE_DAY_DESCRIPTION = "Günü daralt"
    const val MANAGE_BOOKING_COMING_SOON = "Rezervasyon yönetimi yakında kullanıma sunulacak."

    // Flight Section
    const val FLIGHT_TITLE = "Uçuş Bilgileri"
    const val FLIGHT_AIRLINE_LABEL = "Havayolu"
    const val FLIGHT_NUMBER_LABEL = "Uçuş Numarası"
    const val FLIGHT_DEPARTURE_LABEL = "Kalkış"
    const val FLIGHT_ARRIVAL_LABEL = "Varış"
    const val FLIGHT_TERMINAL_LABEL = "Terminal"
    const val FLIGHT_GATE_LABEL = "Kapı"
    const val FLIGHT_GATE_TBD = "Henüz belirlenmedi"

    // Hotel Section
    const val HOTEL_TITLE = "Konaklama"
    const val HOTEL_ADDRESS_LABEL = "Adres"
    const val HOTEL_ROOM_LABEL = "Oda Tipi"
    const val HOTEL_CHECK_IN_LABEL = "Giriş"
    const val HOTEL_CHECK_OUT_LABEL = "Çıkış"

    // Weather Section
    const val WEATHER_TITLE = "Hava Durumu Tahmini"

    // Budget Section
    const val BUDGET_TITLE = "Bütçe Özeti"
    const val BUDGET_SPENT_RING_LABEL = "HARCANAN"
    fun budgetSpentLabel(symbol: String, amount: Int): String = "Harcanan: $symbol$amount"
    fun budgetLimitLabel(symbol: String, amount: Int): String = "Limit: $symbol$amount"
    const val BUDGET_ACCOMMODATION_LABEL = "Konaklama"
    const val BUDGET_TRANSPORTATION_LABEL = "Ulaşım"
    const val BUDGET_FOOD_LABEL = "Yemek"
    const val BUDGET_ACTIVITIES_LABEL = "Aktiviteler"

    // Packing Checklist Section
    const val PACKING_TITLE = "Paketleme Listesi"
    fun packingProgressLabel(checked: Int, total: Int): String = "$checked / $total"
    const val PACKING_VIEW_FULL_LIST = "Tüm Listeyi Görüntüle"

    // Travel Tasks Section (Planner integration — Task is Planner's, Travel only references it)
    const val TASKS_TITLE = "Seyahat Görevleri"
    const val TASKS_EMPTY_TITLE = "Bu seyahat için göreviniz yok"
    const val TASKS_EMPTY_DESCRIPTION = "Hazırlık için bir görev oluşturun."
    const val TASKS_ADD_ACTION = "Görev Ekle"

    // Travel Documents Section
    const val DOCUMENTS_TITLE = "Seyahat Belgeleri"
    const val DOCUMENT_PASSPORT = "Pasaport"
    const val DOCUMENT_VISA = "Vize"
    const val DOCUMENT_INSURANCE = "Seyahat Sigortası"
    const val DOCUMENT_BOARDING_PASS = "Biniş Kartı"
    const val DOCUMENT_STATUS_READY = "Hazır"
    const val DOCUMENT_STATUS_PENDING = "Beklemede"
    const val DOCUMENT_STATUS_MISSING = "Eksik"
    const val DOCUMENT_COMING_SOON = "Belge görüntüleyici yakında kullanıma sunulacak."

    // Notes Section
    const val NOTES_TITLE = "Notlarım"
    const val NOTES_PLACEHOLDER = "Bu seyahatle ilgili notlarınızı buraya ekleyin..."

    // Screen states
    const val DETAIL_LOAD_ERROR_MESSAGE = "Seyahat detayı yüklenemedi. Lütfen tekrar deneyin."
    const val DETAIL_EMPTY_TITLE = "Seyahat bulunamadı"
    const val DETAIL_EMPTY_DESCRIPTION = "Bu seyahat kaldırılmış veya artık mevcut değil."

    // --- Create Travel (AI) (create-travel.png) ---

    const val CREATE_TITLE = "AI Seyahat Planlayıcı"
    const val CREATE_SUBTITLE = "Birkaç adımda size özel bir seyahat planı oluşturalım."

    // Destination Section
    const val DESTINATION_TITLE = "Nereye gitmek istiyorsunuz?"
    const val DESTINATION_COUNTRY_LABEL = "Ülke"
    const val DESTINATION_COUNTRY_PLACEHOLDER = "örn. Japonya"
    const val DESTINATION_CITY_LABEL = "Şehir"
    const val DESTINATION_CITY_PLACEHOLDER = "örn. Kyoto"

    // Travel Dates Section
    const val DATES_TITLE = "Seyahat Tarihleri"
    const val DATES_START_LABEL = "Başlangıç Tarihi"
    const val DATES_END_LABEL = "Bitiş Tarihi"
    const val DATES_PLACEHOLDER = "GG.AA.YYYY"

    // Travel Style Section
    const val STYLE_TITLE = "Seyahat Tarzı"
    const val STYLE_RELAX = "Rahat"
    const val STYLE_ADVENTURE = "Macera"
    const val STYLE_LUXURY = "Lüks"
    const val STYLE_FAMILY = "Aile"
    const val STYLE_BUSINESS = "İş"

    // Budget Input Section
    const val BUDGET_INPUT_TITLE = "Bütçe"
    const val BUDGET_INPUT_LABEL = "Tahmini Bütçe"
    const val BUDGET_INPUT_PLACEHOLDER = "örn. 15000"

    // Companion Section
    const val COMPANION_TITLE = "Seyahat Arkadaşları"
    const val COMPANION_SOLO = "Tek Başıma"
    const val COMPANION_COUPLE = "Eşimle"
    const val COMPANION_FAMILY = "Ailemle"
    const val COMPANION_FRIENDS = "Arkadaşlarımla"

    // Transportation Section (part of Accommodation/Transportation step)
    const val TRANSPORTATION_TITLE = "Ulaşım"
    const val TRANSPORTATION_FLIGHT = "Uçak"
    const val TRANSPORTATION_TRAIN = "Tren"
    const val TRANSPORTATION_CAR = "Araba"
    const val TRANSPORTATION_ANY = "Fark Etmez"

    // Accommodation Preference Section
    const val ACCOMMODATION_PREFERENCE_TITLE = "Konaklama Tercihi"
    const val ACCOMMODATION_HOTEL = "Otel"
    const val ACCOMMODATION_RESORT = "Resort"
    const val ACCOMMODATION_BOUTIQUE = "Butik Otel"
    const val ACCOMMODATION_HOSTEL = "Hostel"

    // Notes Section (Create Travel wording)
    const val CREATE_NOTES_LABEL = "Ek Notlar"
    const val CREATE_NOTES_PLACEHOLDER = "Eklemek istediğiniz özel bir istek var mı?"

    // AI Generation Section
    const val AI_GENERATION_CARD_TITLE = "Yapay Zeka Seyahat Planlayıcı"
    const val AI_GENERATION_CARD_DESCRIPTION =
        "Girdiğiniz bilgilere göre size özel bir itinerer, bütçe ve paketleme listesi oluşturalım."
    const val GENERATE_TRIP_BUTTON = "AI Seyahat Planı Oluştur"
    const val GENERATING_LABEL = "İtinerer, paket listesi ve bütçe hazırlanıyor..."

    // Generated Preview
    const val PREVIEW_TITLE = "Oluşturulan Seyahat Planı"
    const val TRAVEL_TIPS_TITLE = "Seyahat İpuçları"

    // Actions
    const val ACTION_EDIT = "Düzenle"
    const val ACTION_REGENERATE = "Yeniden Oluştur"
    const val ACTION_SAVE_TRIP = "Seyahati Kaydet"

    // Screen states
    const val CREATE_VALIDATION_ERROR_MESSAGE = "Lütfen devam etmeden önce tüm zorunlu alanları doldurun."
    const val CREATE_GENERATION_ERROR_MESSAGE = "Seyahat planı oluşturulamadı. Lütfen tekrar deneyin."
    const val CREATE_SAVE_ERROR_MESSAGE = "Seyahat kaydedilemedi. Lütfen tekrar deneyin."
}
