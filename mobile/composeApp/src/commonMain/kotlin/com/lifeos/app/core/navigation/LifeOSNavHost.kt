package com.lifeos.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeos.app.features.auth.presentation.navigation.AUTH_GRAPH_ROUTE
import com.lifeos.app.features.auth.presentation.navigation.authGraph

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
fun LifeOSNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AUTH_GRAPH_ROUTE,
    ) {
        authGraph(navController)

        composable(Destination.Dashboard.route) {
            MainScreen()
        }
    }
}
