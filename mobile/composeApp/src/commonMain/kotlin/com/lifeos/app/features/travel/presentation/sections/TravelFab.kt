package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppFab
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Yeni Seyahat" (New Trip) FAB (travel-list.png). A thin wrapper over
 * [AppFab] — "Yeni Seyahat" is the accessibility label; the mockup's FAB
 * itself is icon-only, matching every other FAB in the Design System.
 */
@Composable
fun TravelFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppFab(
        contentDescription = TravelStrings.FAB_NEW_TRIP,
        onClick = onClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun TravelFabPreview() {
    LifeOSTheme {
        TravelFab(onClick = {})
    }
}
