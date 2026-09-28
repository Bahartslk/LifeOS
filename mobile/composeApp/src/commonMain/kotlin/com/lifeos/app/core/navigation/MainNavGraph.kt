package com.lifeos.app.core.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifeos.app.features.ai.presentation.AiAssistantRoute
import com.lifeos.app.features.home.presentation.HomeRoute
import com.lifeos.app.features.planner.presentation.CalendarRoute
import com.lifeos.app.features.planner.presentation.CreateTaskRoute
import com.lifeos.app.features.planner.presentation.PlannerRoute
import com.lifeos.app.features.planner.presentation.TaskDetailRoute
import com.lifeos.app.features.profile.presentation.ProfileRoute
import com.lifeos.app.features.travel.presentation.CreateTripRoute
import com.lifeos.app.features.travel.presentation.TravelDetailRoute
import com.lifeos.app.features.travel.presentation.TravelRoute

/**
 * Registers the five main-navigation destinations plus Travel Detail and
 * Create Travel (AI) — non-tab destinations pushed onto the Travel tab's own
 * back stack, not sixth/seventh bottom-nav items. Home Dashboard, Travel
 * (List, Detail, Create Travel AI), Planner Dashboard, and AI Assistant
 * (its Planner-backed context dashboard — no LLM provider wired yet), and
 * Profile (settings + real local theme persistence, everything else a
 * placeholder action) all register their real screens now, the same way
 * `features/auth/presentation/navigation/AuthNavGraph.kt` registers
 * Authentication's real screens.
 *
 * [navController] is the bottom-nav-owned controller from [MainScreen] —
 * Home's quick actions switch tabs through it via [navigateToMainTab], the
 * same tab-switch semantics [AppBottomNavigation] itself uses, so tapping
 * "Seyahat Oluştur" from Home lands exactly where tapping the Travel tab
 * would.
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    onLoggedOut: () -> Unit,
) {
    composable(Destination.Dashboard.route) {
        HomeRoute(
            onNavigateToTravel = { navController.navigateToMainTab(Destination.Travel.route) },
            onNavigateToPlanner = { navController.navigateToMainTab(Destination.Planner.route) },
            onNavigateToAiChat = { navController.navigateToMainTab(Destination.AiChat.route) },
            onNavigateToProfile = { navController.navigateToMainTab(Destination.Profile.route) },
            onNavigateToTaskDetail = { taskId ->
                navController.navigate(Destination.TaskDetail.createRoute(taskId))
            },
            onNavigateToCreateTask = { navController.navigate(Destination.CreateTask.route) },
        )
    }
    composable(Destination.Travel.route) {
        TravelRoute(
            onNavigateToTripDetail = { tripId ->
                navController.navigate(Destination.TripDetail.createRoute(tripId))
            },
            onNavigateToCreateTravel = { navController.navigate(Destination.CreateTravel.route) },
        )
    }
    composable(
        route = Destination.TripDetail.route,
        arguments = listOf(navArgument(Destination.TripDetail.ARG_TRIP_ID) { type = NavType.StringType }),
    ) { backStackEntry ->
        val tripId = backStackEntry.arguments?.getString(Destination.TripDetail.ARG_TRIP_ID).orEmpty()
        TravelDetailRoute(
            tripId = tripId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToTaskDetail = { taskId ->
                navController.navigate(Destination.TaskDetail.createRoute(taskId))
            },
            onNavigateToCreateTask = { navController.navigate(Destination.CreateTask.route) },
        )
    }
    composable(Destination.CreateTravel.route) {
        CreateTripRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToTripDetail = { tripId ->
                navController.navigate(Destination.TripDetail.createRoute(tripId)) {
                    popUpTo(Destination.Travel.route)
                }
            },
        )
    }
    composable(Destination.Planner.route) {
        PlannerRoute(
            onNavigateToProfile = { navController.navigateToMainTab(Destination.Profile.route) },
            onNavigateToTaskDetail = { taskId ->
                navController.navigate(Destination.TaskDetail.createRoute(taskId))
            },
            onNavigateToCreateTask = { navController.navigate(Destination.CreateTask.route) },
            onNavigateToCalendar = { navController.navigate(Destination.Calendar.route) },
        )
    }
    composable(
        route = Destination.TaskDetail.route,
        arguments = listOf(navArgument(Destination.TaskDetail.ARG_TASK_ID) { type = NavType.StringType }),
    ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString(Destination.TaskDetail.ARG_TASK_ID).orEmpty()
        TaskDetailRoute(
            taskId = taskId,
            onNavigateBack = { navController.popBackStack() },
        )
    }
    composable(Destination.CreateTask.route) {
        CreateTaskRoute(
            onNavigateBack = { navController.popBackStack() },
        )
    }
    composable(Destination.Calendar.route) {
        CalendarRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToTaskDetail = { taskId ->
                navController.navigate(Destination.TaskDetail.createRoute(taskId))
            },
            onNavigateToCreateTask = { navController.navigate(Destination.CreateTask.route) },
        )
    }
    composable(Destination.AiChat.route) {
        AiAssistantRoute(
            onNavigateToTaskDetail = { taskId ->
                navController.navigate(Destination.TaskDetail.createRoute(taskId))
            },
            onNavigateToCreateTask = { navController.navigate(Destination.CreateTask.route) },
        )
    }
    composable(Destination.Profile.route) { ProfileRoute(onLoggedOut = onLoggedOut) }
}

/**
 * Switches to a main-nav tab with the standard bottom-nav semantics (save
 * and restore each tab's own back stack, never stack duplicate copies of
 * the same tab). Shared by [AppBottomNavigation][com.lifeos.app.core.designsystem.components.AppBottomNavigation]'s
 * click handling in [MainScreen] and by Home's quick actions above, so the
 * two never drift into different navigation behaviors.
 */
fun NavHostController.navigateToMainTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().route!!) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
