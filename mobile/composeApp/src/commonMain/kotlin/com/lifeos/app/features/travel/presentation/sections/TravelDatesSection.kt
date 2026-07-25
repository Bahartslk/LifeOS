package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Travel Dates" (create-travel.png / this task's screen content). Dates are
 * plain text labels, not a real date picker — matching how every existing
 * [Trip][com.lifeos.app.features.travel.domain.model.Trip] already models
 * dates as a display string (`dateRangeLabel`), never a date/time type.
 */
@Composable
fun TravelDatesSection(
    startDate: String,
    onStartDateChanged: (String) -> Unit,
    endDate: String,
    onEndDateChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.DATES_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
        ) {
            AppTextField(
                value = startDate,
                onValueChange = onStartDateChanged,
                label = TravelStrings.DATES_START_LABEL,
                placeholder = TravelStrings.DATES_PLACEHOLDER,
                modifier = Modifier.weight(1f),
            )
            AppTextField(
                value = endDate,
                onValueChange = onEndDateChanged,
                label = TravelStrings.DATES_END_LABEL,
                placeholder = TravelStrings.DATES_PLACEHOLDER,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun TravelDatesSectionPreview() {
    LifeOSTheme {
        TravelDatesSection(
            startDate = "15.09.2026",
            onStartDateChanged = {},
            endDate = "22.09.2026",
            onEndDateChanged = {},
        )
    }
}
