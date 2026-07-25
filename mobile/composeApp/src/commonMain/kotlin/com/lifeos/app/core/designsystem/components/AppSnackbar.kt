package com.lifeos.app.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBright

/**
 * Themed snackbar content — a dark, rounded surface with a violet action
 * label — for use as the `snackbar` slot of a Material 3 `Scaffold`'s
 * [SnackbarHost]:
 *
 * ```
 * Scaffold(snackbarHost = { SnackbarHost(hostState) { AppSnackbar(it) } })
 * ```
 */
@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        shape = LifeOSShapes.medium,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        actionColor = LifeOSVioletBright,
    )
}

/**
 * Convenience host wiring [AppSnackbar]'s styling into a [SnackbarHost],
 * so screens only need to provide a [SnackbarHostState].
 */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        AppSnackbar(data)
    }
}
