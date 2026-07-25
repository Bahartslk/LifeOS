package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.OptionChipRow
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.AccommodationPreference
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Accommodation Preference" (create-travel.png / this task's screen
 * content). A single-choice [OptionChipRow] over [AccommodationPreference] —
 * distinct from Travel Detail's existing `HotelSection` (which displays a
 * *booked* [com.lifeos.app.features.travel.domain.model.AccommodationInfo],
 * not a preference to choose from).
 */
@Composable
fun AccommodationSection(
    selectedPreference: AccommodationPreference,
    onPreferenceSelected: (AccommodationPreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.ACCOMMODATION_PREFERENCE_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        OptionChipRow(
            options = AccommodationPreference.entries,
            selectedOption = selectedPreference,
            labelFor = { it.toLabel() },
            onOptionSelected = onPreferenceSelected,
        )
    }
}

private fun AccommodationPreference.toLabel(): String = when (this) {
    AccommodationPreference.HOTEL -> TravelStrings.ACCOMMODATION_HOTEL
    AccommodationPreference.RESORT -> TravelStrings.ACCOMMODATION_RESORT
    AccommodationPreference.BOUTIQUE -> TravelStrings.ACCOMMODATION_BOUTIQUE
    AccommodationPreference.HOSTEL -> TravelStrings.ACCOMMODATION_HOSTEL
}

@Preview
@Composable
private fun AccommodationSectionPreview() {
    LifeOSTheme {
        AccommodationSection(selectedPreference = AccommodationPreference.BOUTIQUE, onPreferenceSelected = {})
    }
}
