package com.lifeos.app.core.navigation

/**
 * Route contracts for the app's navigation graph, matching the canonical
 * screen names in docs/05-screen-inventory.md. Declaring routes centrally
 * lets each feature register its own composables against a shared, stable
 * contract instead of inventing its own route strings.
 *
 * This bootstrap declares routes only — no screen composables are
 * implemented against them yet. See LifeOSNavHost.kt.
 */
sealed interface Destination {
    val route: String

    // Authentication stack — docs/05-screen-inventory.md#authentication
    data object Splash : Destination {
        override val route: String = "auth/splash"
    }
    data object Onboarding : Destination {
        override val route: String = "auth/onboarding"
    }
    data object SignIn : Destination {
        override val route: String = "auth/sign-in"
    }
    data object SignUp : Destination {
        override val route: String = "auth/sign-up"
    }
    data object ForgotPassword : Destination {
        override val route: String = "auth/forgot-password"
    }

    // Bottom navigation — docs/05-screen-inventory.md#navigation-map
    data object Dashboard : Destination {
        override val route: String = "home/dashboard"
    }
    data object Travel : Destination {
        override val route: String = "travel/my-trips"
    }

    /**
     * A parameterized destination: [route] is the pattern registered with
     * `NavHost` (`{tripId}` as a placeholder), while [createRoute] builds
     * the concrete, navigable path for a specific trip. Every other
     * [Destination] is a fixed singleton route with no arguments — this is
     * the first to need one, added for Travel Detail rather than as
     * speculative infrastructure.
     */
    data object TripDetail : Destination {
        override val route: String = "travel/trip-detail/{tripId}"
        const val ARG_TRIP_ID = "tripId"
        fun createRoute(tripId: String): String = "travel/trip-detail/$tripId"
    }

    /** "Create Travel (AI)" — pushed onto the Travel tab's back stack, same as [TripDetail]. */
    data object CreateTravel : Destination {
        override val route: String = "travel/create"
    }

    data object Planner : Destination {
        override val route: String = "planner/task-list"
    }

    /** Task Detail — a parameterized destination, mirroring [TripDetail]'s exact `{arg}`-plus-`createRoute` shape. */
    data object TaskDetail : Destination {
        override val route: String = "planner/task-detail/{taskId}"
        const val ARG_TASK_ID = "taskId"
        fun createRoute(taskId: String): String = "planner/task-detail/$taskId"
    }

    /** "Create Task" — pushed onto the Planner tab's back stack, same as [CreateTravel] for Travel. */
    data object CreateTask : Destination {
        override val route: String = "planner/create"
    }

    /** Planner Calendar — pushed onto the Planner tab's back stack, same as [CreateTask]. */
    data object Calendar : Destination {
        override val route: String = "planner/calendar"
    }

    data object AiChat : Destination {
        override val route: String = "ai/chat"
    }
    data object Profile : Destination {
        override val route: String = "profile/overview"
    }
}
