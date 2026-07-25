package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.BudgetSummary
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Budget Overview" (travel-details.png: the 45%-spent ring). [spentPercent]
 * is computed here, in presentation, rather than carried on [BudgetSummary]
 * itself — the same domain-stays-pure-data precedent `OverviewStats`
 * established in Home.
 */
@Composable
fun BudgetSection(
    budget: BudgetSummary,
    modifier: Modifier = Modifier,
) {
    val spentPercent = budget.spentPercent()

    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = TravelStrings.BUDGET_TITLE,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))

        Box(modifier = Modifier.size(RING_SIZE), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { spentPercent / 100f },
                modifier = Modifier.size(RING_SIZE),
                strokeWidth = RING_STROKE_WIDTH,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$spentPercent%", style = LifeOSTextStyles.statDisplay)
                Text(
                    text = TravelStrings.BUDGET_SPENT_RING_LABEL,
                    style = LifeOSTextStyles.overline,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = TravelStrings.budgetSpentLabel(budget.currencySymbol, budget.spentAmount),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = TravelStrings.budgetLimitLabel(budget.currencySymbol, budget.totalBudget),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        LinearProgressIndicator(
            progress = { spentPercent / 100f },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
            BudgetBreakdownRow(TravelStrings.BUDGET_ACCOMMODATION_LABEL, budget.currencySymbol, budget.accommodationCost)
            BudgetBreakdownRow(TravelStrings.BUDGET_TRANSPORTATION_LABEL, budget.currencySymbol, budget.transportationCost)
            BudgetBreakdownRow(TravelStrings.BUDGET_FOOD_LABEL, budget.currencySymbol, budget.foodCost)
            BudgetBreakdownRow(TravelStrings.BUDGET_ACTIVITIES_LABEL, budget.currencySymbol, budget.activitiesCost)
        }
    }
}

@Composable
private fun BudgetBreakdownRow(label: String, currencySymbol: String, amount: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "$currencySymbol$amount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun BudgetSummary.spentPercent(): Int =
    if (totalBudget == 0) 0 else (spentAmount * 100) / totalBudget

private val RING_SIZE = 140.dp
private val RING_STROKE_WIDTH = 10.dp

@Preview
@Composable
private fun BudgetSectionPreview() {
    LifeOSTheme {
        BudgetSection(
            budget = BudgetSummary(
                totalBudget = 2800,
                spentAmount = 1250,
                accommodationCost = 600,
                transportationCost = 350,
                foodCost = 180,
                activitiesCost = 120,
            ),
        )
    }
}
