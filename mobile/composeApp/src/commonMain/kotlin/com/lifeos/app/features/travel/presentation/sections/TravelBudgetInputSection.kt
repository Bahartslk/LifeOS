package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Budget" (create-travel.png / this task's screen content). Named
 * [TravelBudgetInputSection] rather than `BudgetSection` — that name is
 * already taken by Travel Detail's *display* section (a spent/limit ring
 * for an existing trip); this one is a plain numeric *input*, a distinct
 * responsibility that deserves its own name rather than overloading one
 * composable for two unrelated jobs.
 */
@Composable
fun TravelBudgetInputSection(
    budgetAmountText: String,
    onBudgetAmountChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.BUDGET_INPUT_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppTextField(
            value = budgetAmountText,
            onValueChange = onBudgetAmountChanged,
            label = TravelStrings.BUDGET_INPUT_LABEL,
            placeholder = TravelStrings.BUDGET_INPUT_PLACEHOLDER,
            keyboardType = KeyboardType.Number,
        )
    }
}

@Preview
@Composable
private fun TravelBudgetInputSectionPreview() {
    LifeOSTheme {
        TravelBudgetInputSection(budgetAmountText = "15000", onBudgetAmountChanged = {})
    }
}
