package com.lifeos.app.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Collects a ViewModel's one-shot action [Flow] for as long as this
 * composable is in the composition, forwarding each value to [onAction] —
 * the exact `LaunchedEffect(Unit) { actions.collectLatest { ... } }` block
 * every feature's Route composable repeated verbatim (Splash, Onboarding,
 * Login, Register, Forgot Password, Home, Travel, Travel Detail, Create
 * Travel), factored out once the duplication reached nine call sites.
 *
 * `collectLatest`, not `collect`: if [onAction] is still suspended on one
 * action (e.g. a Snackbar mid-display) when a newer action arrives, the
 * newer one wins rather than queuing behind the older one.
 */
@Composable
fun <T> CollectActions(actions: Flow<T>, onAction: suspend (T) -> Unit) {
    LaunchedEffect(actions) {
        actions.collectLatest(onAction)
    }
}
