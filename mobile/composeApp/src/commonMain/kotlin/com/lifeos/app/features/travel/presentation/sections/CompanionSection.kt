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
import com.lifeos.app.features.travel.domain.model.TravelCompanions
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Travel Companions" (create-travel.png / this task's screen content). A
 * single-choice [OptionChipRow] over [TravelCompanions].
 */
@Composable
fun CompanionSection(
    selectedCompanions: TravelCompanions,
    onCompanionsSelected: (TravelCompanions) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.COMPANION_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        OptionChipRow(
            options = TravelCompanions.entries,
            selectedOption = selectedCompanions,
            labelFor = { it.toLabel() },
            onOptionSelected = onCompanionsSelected,
        )
    }
}

private fun TravelCompanions.toLabel(): String = when (this) {
    TravelCompanions.SOLO -> TravelStrings.COMPANION_SOLO
    TravelCompanions.COUPLE -> TravelStrings.COMPANION_COUPLE
    TravelCompanions.FAMILY -> TravelStrings.COMPANION_FAMILY
    TravelCompanions.FRIENDS -> TravelStrings.COMPANION_FRIENDS
}

@Preview
@Composable
private fun CompanionSectionPreview() {
    LifeOSTheme {
        CompanionSection(selectedCompanions = TravelCompanions.COUPLE, onCompanionsSelected = {})
    }
}
