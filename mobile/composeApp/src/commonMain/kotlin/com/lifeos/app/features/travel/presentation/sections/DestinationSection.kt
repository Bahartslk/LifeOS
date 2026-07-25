package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
 * "Destination" (create-travel.png / this task's screen content): country
 * and city free-text inputs. Reuses [AppTextField] rather than a new field
 * component — same pattern as every other form field on this screen.
 */
@Composable
fun DestinationSection(
    country: String,
    onCountryChanged: (String) -> Unit,
    city: String,
    onCityChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.DESTINATION_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg)) {
            AppTextField(
                value = country,
                onValueChange = onCountryChanged,
                label = TravelStrings.DESTINATION_COUNTRY_LABEL,
                placeholder = TravelStrings.DESTINATION_COUNTRY_PLACEHOLDER,
                modifier = Modifier.fillMaxWidth(),
            )
            AppTextField(
                value = city,
                onValueChange = onCityChanged,
                label = TravelStrings.DESTINATION_CITY_LABEL,
                placeholder = TravelStrings.DESTINATION_CITY_PLACEHOLDER,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun DestinationSectionPreview() {
    LifeOSTheme {
        DestinationSection(
            country = "Japonya",
            onCountryChanged = {},
            city = "Kyoto",
            onCityChanged = {},
        )
    }
}
