package com.lifeos.app.features.travel.domain.model

/**
 * The user's input to "Create Travel (AI)" (create-travel.png's underlying
 * intent, expressed here as a structured form rather than free text — see
 * this feature's "Deviations from Stitch" note). Framework-agnostic: no
 * dependency on any particular AI provider's request shape, so it can be
 * mapped to a real LLM prompt later without this type changing.
 */
data class TripGenerationRequest(
    val destinationCity: String,
    val destinationCountry: String,
    val startDateLabel: String,
    val endDateLabel: String,
    val travelStyle: TravelStyle,
    val budgetAmount: Int,
    val currencySymbol: String = "$",
    val companions: TravelCompanions,
    val transportation: TransportationType,
    val accommodationPreference: AccommodationPreference,
    val additionalNotes: String,
)

enum class TravelStyle {
    RELAX,
    ADVENTURE,
    LUXURY,
    FAMILY,
    BUSINESS,
}

enum class TravelCompanions {
    SOLO,
    COUPLE,
    FAMILY,
    FRIENDS,
}

enum class TransportationType {
    FLIGHT,
    TRAIN,
    CAR,
    ANY,
}

enum class AccommodationPreference {
    HOTEL,
    RESORT,
    BOUTIQUE,
    HOSTEL,
}
