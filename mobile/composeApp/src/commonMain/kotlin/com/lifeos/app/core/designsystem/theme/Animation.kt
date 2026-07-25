package com.lifeos.app.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Shared motion constants so transitions (bottom sheet slide-in, snackbar
 * fade, skeleton shimmer, AI segmented loading bar in create-travel.png)
 * feel consistent rather than each feature picking its own durations.
 */
object LifeOSMotion {
    const val DURATION_FAST_MS = 150
    const val DURATION_MEDIUM_MS = 300
    const val DURATION_SLOW_MS = 500

    /** For repeating/ambient animation (shimmer sweep, AI "thinking" dots). */
    const val DURATION_AMBIENT_MS = 1200

    /** Material 3 standard easing, used for most enter/exit transitions. */
    val easingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** A slightly more expressive easing for premium, branded moments
     * (the FAB appearing, a card entering) — a bit more overshoot-free
     * deceleration than the standard curve. */
    val easingEmphasized: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
}
