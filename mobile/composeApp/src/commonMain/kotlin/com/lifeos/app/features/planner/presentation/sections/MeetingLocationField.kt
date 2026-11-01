package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** "Yer", shown only for meetings. Up to 200 characters, like the backend. */
@Composable
fun MeetingLocationField(
    location: String,
    onLocationChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = location,
            onValueChange = { onLocationChanged(it.take(MAX_LOCATION_LENGTH)) },
            label = PlannerStrings.LOCATION_LABEL,
            placeholder = PlannerStrings.LOCATION_PLACEHOLDER,
            leadingIcon = Icons.Filled.Place,
        )
    }
}

private const val MAX_LOCATION_LENGTH = 200

@Preview
@Composable
private fun MeetingLocationFieldPreview() {
    LifeOSTheme {
        MeetingLocationField(location = "Ofis, toplantı odası 2", onLocationChanged = {})
    }
}
