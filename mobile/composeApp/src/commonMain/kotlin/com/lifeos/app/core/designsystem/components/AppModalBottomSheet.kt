package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.theme.LifeOSBottomSheetShape
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * Modal bottom sheet, shaped to match the same large-radius, soft-shadow
 * surface language as [AppCard] (rounded top corners only). Used for
 * contextual actions/forms that shouldn't take over the full screen — e.g.
 * a trip's quick-actions menu, a filter panel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = LifeOSBottomSheetShape,
    ) {
        Column(
            modifier = Modifier.padding(
                start = LifeOSSpacing.lg,
                end = LifeOSSpacing.lg,
                bottom = LifeOSSpacing.xl,
            ),
        ) {
            content()
        }
    }
}
