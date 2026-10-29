package com.lifeos.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.auth.presentation.navigation.AUTH_GRAPH_ROUTE
import com.lifeos.app.features.auth.presentation.navigation.authGraph
import com.lifeos.app.features.auth.presentation.session.SessionGuardViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root navigation host: two top-level, mutually isolated graphs.
 *
 * - [AUTH_GRAPH_ROUTE] (`authGraph`) — Splash, Onboarding, Login, Register,
 *   Forgot Password. The app always starts here; Splash decides where to
 *   go next (docs/04-user-flow.md#entry-points).
 * - [Destination.Dashboard]'s graph (`mainGraph`, via [MainScreen]) — the
 *   five main-navigation destinations (Home, Travel, Planner, AI Assistant,
 *   Profile) plus Travel Detail and Create Travel (AI); every destination
 *   now has a real screen.
 *
 * Authentication has no compile-time dependency on the main graph beyond
 * the opaque [Destination] route strings it navigates to on success —
 * satisfying "Authentication must be isolated from future modules."
 */
@Composable
fun LifeOSNavHost(sessionGuard: SessionGuardViewModel = koinViewModel()) {
    val navController = rememberNavController()

    // Clears the whole back stack (the main graph and every ViewModel scoped
    // to it) so system back from Login can never return to an authenticated
    // screen.
    val navigateToSignIn = {
        navController.navigate(Destination.SignIn.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    // The session expired while the app was running (the backend rejected
    // the refresh token): leave the main graph instead of letting every
    // screen show an error it can't recover from. Already on an auth screen
    // means there's nothing to leave.
    CollectActions(sessionGuard.sessionExpired) {
        if (navController.currentDestination?.route == Destination.Dashboard.route) {
            navigateToSignIn()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AUTH_GRAPH_ROUTE,
    ) {
        authGraph(navController)

        composable(Destination.Dashboard.route) {
            MainScreen(onLoggedOut = navigateToSignIn)
        }
    }
}
