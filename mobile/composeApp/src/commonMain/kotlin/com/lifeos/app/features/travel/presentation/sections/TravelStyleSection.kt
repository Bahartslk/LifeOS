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
import com.lifeos.app.features.travel.domain.model.TravelStyle
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Travel Style" (create-travel.png / this task's screen content: Relax,
 * Adventure, Luxury, Family, Business). A single-choice [OptionChipRow] over
 * [TravelStyle] — the same domain enum
 * [com.lifeos.app.features.travel.data.datasource.FakeTripGenerationDataSource]
 * branches on to pick itinerary/packing templates, so the UI's choice and
 * the AI generation logic can never drift out of sync.
 */
@Composable
fun TravelStyleSection(
    selectedStyle: TravelStyle,
    onStyleSelected: (TravelStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.STYLE_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        OptionChipRow(
            options = TravelStyle.entries,
            selectedOption = selectedStyle,
            labelFor = { it.toLabel() },
            onOptionSelected = onStyleSelected,
        )
    }
}

private fun TravelStyle.toLabel(): String = when (this) {
    TravelStyle.RELAX -> TravelStrings.STYLE_RELAX
    TravelStyle.ADVENTURE -> TravelStrings.STYLE_ADVENTURE
    TravelStyle.LUXURY -> TravelStrings.STYLE_LUXURY
    TravelStyle.FAMILY -> TravelStrings.STYLE_FAMILY
    TravelStyle.BUSINESS -> TravelStrings.STYLE_BUSINESS
}

@Preview
@Composable
private fun TravelStyleSectionPreview() {
    LifeOSTheme {
        TravelStyleSection(selectedStyle = TravelStyle.ADVENTURE, onStyleSelected = {})
    }
}
