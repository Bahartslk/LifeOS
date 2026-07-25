package com.lifeos.app.features.travel.domain.model

/**
 * "Budget Overview" (travel-details.png: the 45%-spent ring, "$1,250" /
 * "$2,800"). [currencySymbol] defaults to matching the Stitch mockup
 * exactly rather than assuming Turkish Lira, since the mockup itself prices
 * this trip in USD.
 *
 * Deliberately carries no derived "spent percent" property — that's a
 * presentation-layer computation (see `BudgetSection.kt`), matching the
 * same domain-stays-pure-data precedent `OverviewStats` established in
 * the Home feature.
 */
data class BudgetSummary(
    val totalBudget: Int,
    val spentAmount: Int,
    val accommodationCost: Int,
    val transportationCost: Int,
    val foodCost: Int,
    val activitiesCost: Int,
    val currencySymbol: String = "$",
)
