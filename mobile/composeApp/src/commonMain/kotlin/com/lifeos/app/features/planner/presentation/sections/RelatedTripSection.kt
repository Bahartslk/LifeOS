package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Related Trip (placeholder)" (this task's requirement) — shown only for
 * a [com.lifeos.app.features.planner.domain.model.Task] whose
 * [com.lifeos.app.features.planner.domain.model.TaskSource] is `TRAVEL`
 * (the [com.lifeos.app.features.planner.presentation.TaskDetailScreen]
 * decides visibility; this composable stays a pure display concern). No
 * `tripId` exists on [com.lifeos.app.features.planner.domain.model.Task]
 * yet — deliberately not added for this placeholder alone, per this task's
 * "extend Task only if genuinely required" rule — so [onViewTripClick]
 * surfaces a "coming soon" message rather than a real navigation.
 */
@Composable
fun RelatedTripSection(
    onViewTripClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            AppIcon(
                imageVector = Icons.Filled.FlightTakeoff,
                contentDescription = null,
                tint = LifeOSVioletBase,
            )
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = PlannerStrings.RELATED_TRIP_TITLE, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
                Text(
                    text = PlannerStrings.RELATED_TRIP_DESCRIPTION,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppOutlinedButton(text = PlannerStrings.RELATED_TRIP_ACTION, onClick = onViewTripClick)
    }
}

@Preview
@Composable
private fun RelatedTripSectionPreview() {
    LifeOSTheme {
        RelatedTripSection(onViewTripClick = {})
    }
}
