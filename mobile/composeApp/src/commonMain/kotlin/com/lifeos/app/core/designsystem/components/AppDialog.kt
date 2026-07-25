package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.lifeos.app.core.designsystem.theme.LifeOSElevation
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * Base dialog surface: a large-radius card centered over a scrim, matching
 * the same visual language as [AppCard] rather than Material's default
 * (smaller-radius) AlertDialog shape.
 *
 * Deliberately does NOT use Material 3's `AlertDialog` composable — that
 * component lays its `confirmButton`/`dismissButton` slots side-by-side as
 * compact `TextButton`s, which doesn't fit this design system's full-width
 * pill buttons ([AppPrimaryButton], [AppOutlinedButton]). [buttons] instead
 * receives a [Column] scope so actions stack vertically, full-width — see
 * [ConfirmationDialog] for the common case.
 */
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    body: String? = null,
    content: (@Composable () -> Unit)? = null,
    buttons: (@Composable () -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = LifeOSShapes.extraLarge,
            tonalElevation = LifeOSElevation.level3,
        ) {
            Column(
                modifier = Modifier.padding(LifeOSSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
            ) {
                if (title != null) {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                }
                if (body != null) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                content?.invoke()
                if (buttons != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                        buttons()
                    }
                }
            }
        }
    }
}
