package com.lifeos.app.features.travel.domain.model

/** "Travel Documents" (this task's requirement): passport, visa, insurance, boarding pass. */
data class TravelDocument(
    val type: DocumentType,
    val status: DocumentStatus,
)

enum class DocumentType {
    PASSPORT,
    VISA,
    INSURANCE,
    BOARDING_PASS,
}

enum class DocumentStatus {
    READY,
    PENDING,
    MISSING,
}
