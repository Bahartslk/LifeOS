package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A "label above value" row — Travel Detail's Flight/Hotel sections and
 * Task Detail's Task Info card all need several of these (Airline/Flight
 * Number/…, Due Date/Reminder/Status/…). Originally feature-local to
 * Travel; promoted here once Planner needed the identical row shape, per
 * this project's "only extract a Design System component once it's reused
 * by at least two features" rule.
 */
@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
