package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Current Month Header" + "Previous / Next Month navigation" (this
 * feature's requirement) — the same month-label-plus-chevrons row
 * [PlannerCalendarSection] already established for the Dashboard's
 * read-only widget, extracted as its own composable since here the
 * chevrons page real months instead of resolving to a "coming soon"
 * message.
 */
@Composable
fun MonthSelector(
    monthLabel: String,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonthClick) {
                AppIcon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = PlannerStrings.CALENDAR_PREVIOUS_MONTH_DESCRIPTION,
                )
            }
            Text(text = monthLabel, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNextMonthClick) {
                AppIcon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = PlannerStrings.CALENDAR_NEXT_MONTH_DESCRIPTION,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MonthSelectorPreview() {
    LifeOSTheme {
        MonthSelector(monthLabel = "Ekim 2024", onPreviousMonthClick = {}, onNextMonthClick = {})
    }
}
