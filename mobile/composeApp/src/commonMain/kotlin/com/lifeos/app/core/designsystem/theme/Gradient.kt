package com.lifeos.app.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The LifeOS signature violet gradient, used consistently for every
 * "hero" surface across the Stitch designs: the splash background
 * (splash.png), primary buttons (login.png "Login", onboarding-2.png
 * "Next Step"), the Home "Intelligent Hub" card (home.png), and the
 * user's own chat bubbles (ai-assistent.png).
 *
 * There is deliberately only one brand gradient, not one per screen — reuse
 * this everywhere a "premium violet surface" is called for rather than
 * defining a new Brush per feature.
 */
object LifeOSGradients {
    val primary = Brush.linearGradient(
        colors = listOf(LifeOSVioletDeep, LifeOSVioletBright),
    )

    /** A steeper, darker variant for full-bleed hero backgrounds (e.g. Splash). */
    val hero = Brush.linearGradient(
        colors = listOf(LifeOSVioletDeep, Color(0xFFA21CAF)),
    )
}
