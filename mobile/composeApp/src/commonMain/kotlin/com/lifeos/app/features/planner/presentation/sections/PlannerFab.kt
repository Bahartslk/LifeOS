package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppFab
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Yeni Görev" (New Task) FAB (planner.png). A thin wrapper over [AppFab] —
 * "Yeni Görev" is the accessibility label; the mockup's FAB itself is
 * icon-only, matching every other FAB in the Design System.
 */
@Composable
fun PlannerFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppFab(
        contentDescription = PlannerStrings.FAB_NEW_TASK,
        onClick = onClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PlannerFabPreview() {
    LifeOSTheme {
        PlannerFab(onClick = {})
    }
}
