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
import com.lifeos.app.features.travel.domain.model.TransportationType
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Transportation" (create-travel.png / this task's screen content). A
 * single-choice [OptionChipRow] over [TransportationType].
 */
@Composable
fun TransportationSection(
    selectedTransportation: TransportationType,
    onTransportationSelected: (TransportationType) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.TRANSPORTATION_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        OptionChipRow(
            options = TransportationType.entries,
            selectedOption = selectedTransportation,
            labelFor = { it.toLabel() },
            onOptionSelected = onTransportationSelected,
        )
    }
}

private fun TransportationType.toLabel(): String = when (this) {
    TransportationType.FLIGHT -> TravelStrings.TRANSPORTATION_FLIGHT
    TransportationType.TRAIN -> TravelStrings.TRANSPORTATION_TRAIN
    TransportationType.CAR -> TravelStrings.TRANSPORTATION_CAR
    TransportationType.ANY -> TravelStrings.TRANSPORTATION_ANY
}

@Preview
@Composable
private fun TransportationSectionPreview() {
    LifeOSTheme {
        TransportationSection(selectedTransportation = TransportationType.FLIGHT, onTransportationSelected = {})
    }
}
