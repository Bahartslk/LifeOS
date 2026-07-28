package com.lifeos.app.features.travel.data.datasource

import com.lifeos.app.features.travel.domain.model.AccommodationInfo
import com.lifeos.app.features.travel.domain.model.AccommodationPreference
import com.lifeos.app.features.travel.domain.model.AiTripSummary
import com.lifeos.app.features.travel.domain.model.BudgetSummary
import com.lifeos.app.features.travel.domain.model.DailyWeather
import com.lifeos.app.features.travel.domain.model.DayPeriod
import com.lifeos.app.features.travel.domain.model.ItineraryActivity
import com.lifeos.app.features.travel.domain.model.ItineraryDay
import com.lifeos.app.features.travel.domain.model.PackingCategory
import com.lifeos.app.features.travel.domain.model.PackingItem
import com.lifeos.app.features.travel.domain.model.PackingItemIcon
import com.lifeos.app.features.travel.domain.model.TravelCompanions
import com.lifeos.app.features.travel.domain.model.TravelStyle
import com.lifeos.app.features.travel.domain.model.TransportationType
import com.lifeos.app.features.travel.domain.model.Trip
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripGenerationRequest
import com.lifeos.app.features.travel.domain.model.TripStatus
import com.lifeos.app.features.travel.domain.model.WeatherCondition

/**
 * The one place fake *AI-generated* content lives, per this task's "keep
 * fake data inside dedicated fake data sources" rule and "do not hardcode
 * generated content inside the ViewModel." Kept separate from
 * [FakeTravelDataSource] — that class answers "what trips already exist";
 * this one answers "what would the AI propose for a brand-new request" —
 * distinct concerns that would only get confusing if merged into one file.
 *
 * Holds no business logic beyond template selection (which is itself just
 * data shaping, not a decision an AI would need to make correctly) —
 * [com.lifeos.app.features.travel.data.repository.FakeTripGenerationRepository]
 * owns the (currently trivial) orchestration and the simulated latency.
 */
class FakeTripGenerationDataSource {

    fun generateTripDetail(request: TripGenerationRequest, tripId: String): TripDetail {
        val style = request.travelStyle
        val forecastTemperature = temperatureFor(request.destinationCity)
        val trip = Trip(
            id = tripId,
            destinationCity = request.destinationCity,
            destinationCountry = request.destinationCountry,
            dateRangeLabel = "${request.startDateLabel} - ${request.endDateLabel}",
            status = TripStatus.PLANNED,
            coverImageUrl = COVER_IMAGE_URLS[request.destinationCity] ?: DEFAULT_COVER_IMAGE_URL,
            isAiOptimized = true,
            daysUntilStart = null,
            weatherTemperatureCelsius = forecastTemperature,
            photoCount = null,
            travelStyle = style,
        )

        return TripDetail(
            trip = trip,
            aiSummary = AiTripSummary(
                highlightMessage = "${request.destinationCity} için ${style.description()} bir plan hazırladım. " +
                    "${request.companions.description()} seyahat ettiğinizi ve " +
                    "${request.transportation.description()} ile gideceğinizi göz önünde bulundurdum.",
                weatherTemperatureCelsius = forecastTemperature,
                windSpeedKmh = DEFAULT_WIND_SPEED_KMH,
                travelTips = style.toTravelTips(request.destinationCity),
            ),
            itinerary = style.toItinerary(request.destinationCity),
            flight = null,
            accommodation = request.accommodationPreference.toAccommodation(request.destinationCity),
            weatherForecast = listOf(
                DailyWeather("Gün 1", forecastTemperature, WeatherCondition.SUNNY),
                DailyWeather("Gün 2", forecastTemperature - 1, WeatherCondition.CLOUDY),
                DailyWeather("Gün 3", forecastTemperature + 1, WeatherCondition.SUNNY),
            ),
            budget = request.toBudgetSummary(),
            packingCategories = style.toPackingCategories(),
            documents = emptyList(),
            notes = request.additionalNotes,
        )
    }

    /**
     * A fixed 20°C for every destination made the mock forecast an obvious
     * tell that it wasn't actually tied to the selected city (Bug 4). With no
     * real weather provider wired up yet, this derives a stable, plausible
     * mock temperature from the destination name itself — same destination
     * always yields the same value, different destinations plausibly differ.
     */
    private fun temperatureFor(destinationCity: String): Int {
        val hash = destinationCity.trim().lowercase().sumOf { it.code }
        return MIN_FORECAST_TEMPERATURE + (hash % TEMPERATURE_RANGE)
    }

    private fun TravelStyle.description(): String = when (this) {
        TravelStyle.RELAX -> "sakin ve dinlendirici"
        TravelStyle.ADVENTURE -> "macera dolu"
        TravelStyle.LUXURY -> "lüks ve konforlu"
        TravelStyle.FAMILY -> "aile dostu"
        TravelStyle.BUSINESS -> "iş odaklı ve verimli"
    }

    private fun TravelCompanions.description(): String = when (this) {
        TravelCompanions.SOLO -> "tek başınıza"
        TravelCompanions.COUPLE -> "eşinizle"
        TravelCompanions.FAMILY -> "ailenizle"
        TravelCompanions.FRIENDS -> "arkadaşlarınızla"
    }

    private fun TransportationType.description(): String = when (this) {
        TransportationType.FLIGHT -> "uçak"
        TransportationType.TRAIN -> "tren"
        TransportationType.CAR -> "araba"
        TransportationType.ANY -> "en uygun ulaşım"
    }

    private fun TravelStyle.toItinerary(destinationCity: String): List<ItineraryDay> = when (this) {
        TravelStyle.RELAX -> listOf(
            ItineraryDay(
                dayNumber = 1,
                title = "$destinationCity'de Sakinleşme",
                activities = listOf(
                    ItineraryActivity(DayPeriod.MORNING, "Otele yerleşme ve dinlenme"),
                    ItineraryActivity(DayPeriod.AFTERNOON, "Spa ve wellness merkezi"),
                    ItineraryActivity(DayPeriod.EVENING, "Sakin bir sahil yürüyüşü"),
                ),
                photoUrls = emptyList(),
                bookingActionLabel = null,
            ),
        )
        TravelStyle.ADVENTURE -> listOf(
            ItineraryDay(
                dayNumber = 1,
                title = "$destinationCity Macerası Başlıyor",
                activities = listOf(
                    ItineraryActivity(DayPeriod.MORNING, "Doğa yürüyüşü rotası"),
                    ItineraryActivity(DayPeriod.AFTERNOON, "Tırmanış veya rafting aktivitesi"),
                    ItineraryActivity(DayPeriod.EVENING, "Kamp ateşi ve yıldız gözlemi"),
                ),
                photoUrls = emptyList(),
                bookingActionLabel = "Rezervasyonu Yönet",
            ),
        )
        TravelStyle.LUXURY -> listOf(
            ItineraryDay(
                dayNumber = 1,
                title = "$destinationCity'de Ayrıcalıklı Bir Gün",
                activities = listOf(
                    ItineraryActivity(DayPeriod.MORNING, "Özel şoförle şehir turu"),
                    ItineraryActivity(DayPeriod.AFTERNOON, "Michelin yıldızlı restoranda öğle yemeği"),
                    ItineraryActivity(DayPeriod.EVENING, "Özel tekne turu"),
                ),
                photoUrls = emptyList(),
                bookingActionLabel = "Rezervasyonu Yönet",
            ),
        )
        TravelStyle.FAMILY -> listOf(
            ItineraryDay(
                dayNumber = 1,
                title = "$destinationCity'de Aile Günü",
                activities = listOf(
                    ItineraryActivity(DayPeriod.MORNING, "Çocuk dostu müze ziyareti"),
                    ItineraryActivity(DayPeriod.AFTERNOON, "Tema parkı veya hayvanat bahçesi"),
                    ItineraryActivity(DayPeriod.EVENING, "Aile restoranında akşam yemeği"),
                ),
                photoUrls = emptyList(),
                bookingActionLabel = null,
            ),
        )
        TravelStyle.BUSINESS -> listOf(
            ItineraryDay(
                dayNumber = 1,
                title = "$destinationCity İş Programı",
                activities = listOf(
                    ItineraryActivity(DayPeriod.MORNING, "Otele giriş ve toplantı hazırlığı"),
                    ItineraryActivity(DayPeriod.AFTERNOON, "İş görüşmeleri"),
                    ItineraryActivity(DayPeriod.EVENING, "İş yemeği ve networking"),
                ),
                photoUrls = emptyList(),
                bookingActionLabel = null,
            ),
        )
    }

    private fun TravelStyle.toTravelTips(destinationCity: String): List<String> = when (this) {
        TravelStyle.RELAX -> listOf(
            "$destinationCity'de en sakin saatler genellikle sabah erken saatlerdir.",
            "Spa rezervasyonlarınızı seyahatinizden önce yaptırmanızı öneririz.",
        )
        TravelStyle.ADVENTURE -> listOf(
            "Su geçirmez ve katmanlı kıyafetler paketlemeyi unutmayın.",
            "Aktivitelerden önce yerel hava durumunu kontrol edin.",
        )
        TravelStyle.LUXURY -> listOf(
            "Popüler restoranlar için rezervasyonları önceden yaptırın.",
            "Şık mekanlar için kıyafet kurallarını kontrol etmenizi öneririz.",
        )
        TravelStyle.FAMILY -> listOf(
            "Çocuklarla kısa mesafeli aktiviteleri günün başına planlayın.",
            "Aile dostu restoranları önceden araştırmanız zaman kazandırır.",
        )
        TravelStyle.BUSINESS -> listOf(
            "Toplantı yerine yakın bir konaklama seçmek zamandan tasarruf sağlar.",
            "İş görüşmeleri için yerel iş görgü kurallarına göz atmanızı öneririz.",
        )
    }

    private fun TravelStyle.toPackingCategories(): List<PackingCategory> = when (this) {
        TravelStyle.RELAX -> listOf(
            PackingCategory(
                "Genel",
                listOf(
                    PackingItem("gen-swimsuit", "Mayo", false, PackingItemIcon.GENERAL),
                    PackingItem("gen-sunscreen", "Güneş kremi", false, PackingItemIcon.TOILETRIES),
                ),
            ),
        )
        TravelStyle.ADVENTURE -> listOf(
            PackingCategory(
                "Genel",
                listOf(
                    PackingItem("adv-boots", "Yürüyüş botları", false, PackingItemIcon.FOOTWEAR),
                    PackingItem("adv-jacket", "Su geçirmez mont", false, PackingItemIcon.COLD_WEATHER),
                    PackingItem("adv-camera", "Kamera ekipmanı", false, PackingItemIcon.CAMERA),
                ),
            ),
        )
        TravelStyle.LUXURY -> listOf(
            PackingCategory(
                "Genel",
                listOf(
                    PackingItem("lux-formal", "Şık kıyafetler", false, PackingItemIcon.GENERAL),
                    PackingItem("lux-docs", "Rezervasyon belgeleri", false, PackingItemIcon.DOCUMENTS),
                ),
            ),
        )
        TravelStyle.FAMILY -> listOf(
            PackingCategory(
                "Genel",
                listOf(
                    PackingItem("fam-snacks", "Atıştırmalıklar", false, PackingItemIcon.GENERAL),
                    PackingItem("fam-electronics", "Şarj cihazları", false, PackingItemIcon.ELECTRONICS),
                ),
            ),
        )
        TravelStyle.BUSINESS -> listOf(
            PackingCategory(
                "Genel",
                listOf(
                    PackingItem("bus-formal", "İş kıyafetleri", false, PackingItemIcon.GENERAL),
                    PackingItem("bus-electronics", "Dizüstü bilgisayar", false, PackingItemIcon.ELECTRONICS),
                    PackingItem("bus-docs", "Sunum belgeleri", false, PackingItemIcon.DOCUMENTS),
                ),
            ),
        )
    }

    private fun AccommodationPreference.toAccommodation(destinationCity: String): AccommodationInfo {
        val (roomType, hotelSuffix) = when (this) {
            AccommodationPreference.HOTEL -> "Standart Oda" to "Otel"
            AccommodationPreference.RESORT -> "Deluxe Süit" to "Resort"
            AccommodationPreference.BOUTIQUE -> "Butik Oda" to "Butik Otel"
            AccommodationPreference.HOSTEL -> "Paylaşımlı Oda" to "Hostel"
        }
        return AccommodationInfo(
            hotelName = "$destinationCity $hotelSuffix",
            address = "$destinationCity Merkez",
            roomType = roomType,
            checkInLabel = "14:00",
            checkOutLabel = "12:00",
        )
    }

    private fun TripGenerationRequest.toBudgetSummary(): BudgetSummary = BudgetSummary(
        totalBudget = budgetAmount,
        spentAmount = 0,
        accommodationCost = (budgetAmount * ACCOMMODATION_SHARE).toInt(),
        transportationCost = (budgetAmount * TRANSPORTATION_SHARE).toInt(),
        foodCost = (budgetAmount * FOOD_SHARE).toInt(),
        activitiesCost = (budgetAmount * ACTIVITIES_SHARE).toInt(),
        currencySymbol = currencySymbol,
    )

    private companion object {
        const val MIN_FORECAST_TEMPERATURE = 14
        const val TEMPERATURE_RANGE = 15
        const val DEFAULT_WIND_SPEED_KMH = 8
        const val ACCOMMODATION_SHARE = 0.4
        const val TRANSPORTATION_SHARE = 0.25
        const val FOOD_SHARE = 0.2
        const val ACTIVITIES_SHARE = 0.15
        const val DEFAULT_COVER_IMAGE_URL = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=1200"
        val COVER_IMAGE_URLS = mapOf(
            "Kapadokya" to "https://images.unsplash.com/photo-1641731538990-98de926df457?w=1200",
            "Kyoto" to "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=1200",
            "Paris" to "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=1200",
        )
    }
}
