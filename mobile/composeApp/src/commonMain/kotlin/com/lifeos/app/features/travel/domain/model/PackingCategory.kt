package com.lifeos.app.features.travel.domain.model

/**
 * "Packing" checklist (travel-details.png: "Camera gear", "Warm jacket",
 * "Hiking boots", "3 of 8"). Grouped into categories per this task's
 * explicit requirement — the Stitch mockup shows a flat preview list (the
 * first few items of a longer 8-item list, per its own "3 of 8" counter and
 * "View full list" link), consistent with categorization applying to the
 * full list rather than contradicting the preview.
 */
data class PackingCategory(
    val name: String,
    val items: List<PackingItem>,
)

data class PackingItem(
    val id: String,
    val label: String,
    val isChecked: Boolean,
    val icon: PackingItemIcon,
)

enum class PackingItemIcon {
    CAMERA,
    COLD_WEATHER,
    FOOTWEAR,
    DOCUMENTS,
    ELECTRONICS,
    TOILETRIES,
    GENERAL,
}
