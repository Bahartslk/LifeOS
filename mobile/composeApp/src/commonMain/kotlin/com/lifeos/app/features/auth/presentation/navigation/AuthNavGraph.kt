package com.lifeos.app.features.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.navigation.compose.composable
import com.lifeos.app.core.navigation.Destination
import com.lifeos.app.features.auth.presentation.forgotpassword.ForgotPasswordRoute
import com.lifeos.app.features.auth.presentation.login.LoginRoute
import com.lifeos.app.features.auth.presentation.onboarding.OnboardingRoute
import com.lifeos.app.features.auth.presentation.register.RegisterRoute
import com.lifeos.app.features.auth.presentation.resetpassword.ResetPasswordRoute
import com.lifeos.app.features.auth.presentation.splash.SplashRoute

/** The nested graph route Authentication lives under — see [authGraph]. */
const val AUTH_GRAPH_ROUTE = "auth-graph"

/**
 * The complete Authentication navigation graph: Splash, Onboarding, Login,
 * Register, Forgot Password, Reset Password. Registered as a single nested graph in
 * [com.lifeos.app.core.navigation.LifeOSNavHost] so Authentication stays
 * isolated from the future Home/Travel/Planner/AI Assistant/Profile graph —
 * this function is the *only* thing the app shell needs to know about to
 * wire Authentication in, per docs/12-project-architecture.md#feature-first-module-organization.
 */
fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation(startDestination = Destination.Splash.route, route = AUTH_GRAPH_ROUTE) {
        composable(Destination.Splash.route) {
            SplashRoute(
                onNavigateToOnboarding = {
                    navController.navigate(Destination.Onboarding.route) {
                        popUpTo(Destination.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigateClearingAuthGraph(Destination.SignIn.route) },
                onNavigateToHome = { navController.navigateClearingAuthGraph(Destination.Dashboard.route) },
            )
        }

        composable(Destination.Onboarding.route) {
            OnboardingRoute(
                onNavigateToLogin = { navController.navigateClearingAuthGraph(Destination.SignIn.route) },
            )
        }

        composable(Destination.SignIn.route) {
            LoginRoute(
                onNavigateToRegister = { navController.navigate(Destination.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(Destination.ForgotPassword.route) },
                onNavigateToHome = { navController.navigateClearingAuthGraph(Destination.Dashboard.route) },
            )
        }

        composable(Destination.SignUp.route) {
            RegisterRoute(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = { navController.navigateClearingAuthGraph(Destination.Dashboard.route) },
            )
        }

        composable(Destination.ForgotPassword.route) {
            ForgotPasswordRoute(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToResetPassword = { email ->
                    navController.navigate(Destination.ResetPassword.createRoute(email))
                },
            )
        }

        composable(
            route = Destination.ResetPassword.route,
            arguments = listOf(
                navArgument(Destination.ResetPassword.ARG_EMAIL) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            ResetPasswordRoute(
                email = backStackEntry.arguments?.getString(Destination.ResetPassword.ARG_EMAIL).orEmpty(),
                // A fresh sign-in entry (its ViewModel reads the "password reset" notice when created),
                // replacing the old one and both reset screens.
                onPasswordReset = {
                    navController.navigate(Destination.SignIn.route) {
                        popUpTo(Destination.SignIn.route) { inclusive = true }
                    }
                },
            )
        }
    }
}

/**
 * Every path that leaves Authentication for Home clears the entire auth
 * back stack (Splash through wherever the user ended up — Login, Register,
 * or Forgot Password), so the system back button from Home never re-enters
 * Authentication.
 */
private fun NavController.navigateClearingAuthGraph(route: String) {
    navigate(route) {
        popUpTo(Destination.Splash.route) { inclusive = true }
    }
}
