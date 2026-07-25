package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Planner Calendar's top bar (this feature's requirement) — a thin [AppTopBar] wrapper, mirroring [TaskHeader]'s shape. */
@Composable
fun CalendarHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTopBar(
        title = PlannerStrings.CALENDAR_HEADER_TITLE,
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        navigationContentDescription = PlannerStrings.BACK_CONTENT_DESCRIPTION,
        onNavigationClick = onBackClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun CalendarHeaderPreview() {
    LifeOSTheme {
        CalendarHeader(onBackClick = {})
    }
}
