package com.lifeos.app.features.planner.presentation.sections

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
import com.lifeos.app.features.planner.presentation.DURATION_OPTIONS
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** "Süre": how long the task or meeting takes; with a time it gives the meeting's end (10:00–11:00). */
@Composable
fun DurationSection(
    durationMinutes: Int?,
    onDurationSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.DURATION_LABEL, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        OptionChipRow(
            options = DURATION_OPTIONS,
            selectedOption = durationMinutes,
            labelFor = { minutes -> minutes?.let(PlannerStrings::durationLabel) ?: PlannerStrings.DURATION_NONE },
            onOptionSelected = onDurationSelected,
        )
    }
}

@Preview
@Composable
private fun DurationSectionPreview() {
    LifeOSTheme {
        DurationSection(durationMinutes = 60, onDurationSelected = {})
    }
}
