package com.lifeos.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Color tokens extracted directly from the Stitch designs in `design/stitch/`
 * (splash.png, login.png, home.png, ai-assistent.png, travel-list.png,
 * travel-details.png, create-travel.png, planner.png, profile.png), per
 * docs/06-design-system.md#color-palette.
 *
 * Brand identity is a premium violet-to-purple gradient (splash background,
 * primary buttons, the Home "Intelligent Hub" card, AI chat bubbles) with a
 * teal/emerald secondary accent used for success/positive-metric surfaces
 * (productivity score, weather chips, "LifeOS Engine" AI attribution,
 * "COMPLETED" trip badges).
 *
 * The rest of the app depends only on MaterialTheme.colorScheme (or the
 * gradients in Gradient.kt) — never on these constants directly — so this
 * file is the only place that needs to change if the brand palette evolves.
 */

// --- Violet — primary brand color (buttons, links, active states, AI bubbles) ---
val LifeOSVioletDeep = Color(0xFF5B21B6) // splash.png gradient start / login.png button start
val LifeOSVioletBright = Color(0xFF9333EA) // splash.png gradient end / onboarding "Next" button end
val LifeOSVioletBase = Color(0xFF7C3AED) // "LifeOS" wordmark, links ("Forgot Password?", "Create Account")
val LifeOSVioletContainerLight = Color(0xFFEDE7FE) // eyebrow chip backgrounds, quick-action icon circles
val LifeOSVioletContainerDark = Color(0xFF4C1D95) // text/icon tint on top of LifeOSVioletContainerLight

// --- Teal — secondary accent (success, weather, productivity, AI attribution) ---
val LifeOSTealBase = Color(0xFF0F9488) // "Lens Selection Guide" card, productivity icon, weather chip
val LifeOSTealContainerLight = Color(0xFFCCFBF1)
val LifeOSTealContainerDark = Color(0xFF0F766E)

// --- Status — priority / completion badges ---
val LifeOSStatusHighPriority = Color(0xFFDC2626) // "HIGH PRIORITY" chip text (home.png)
val LifeOSStatusHighPriorityContainer = Color(0xFFFEE2E2)
val LifeOSStatusCompleted = Color(0xFF0F9488) // "COMPLETED" badge (travel-list.png memories)

// --- Neutrals ---
val LifeOSSurfaceLight = Color(0xFFF7F6FB) // screen background behind white cards
val LifeOSOnSurfaceLight = Color(0xFF17151F) // headline text
val LifeOSOnSurfaceVariantLight = Color(0xFF6B7280) // supporting/secondary text
val LifeOSOutlineLight = Color(0xFFD8D5E0) // text field borders, dividers

val LifeOSSurfaceDark = Color(0xFF14121B)
val LifeOSOnSurfaceDark = Color(0xFFECEAF2)
val LifeOSOnSurfaceVariantDark = Color(0xFFAAA6B8)
val LifeOSOutlineDark = Color(0xFF4A4658)

// --- Light scheme ---
val md_theme_light_primary = LifeOSVioletBase
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = LifeOSVioletContainerLight
val md_theme_light_onPrimaryContainer = LifeOSVioletContainerDark
val md_theme_light_secondary = Color(0xFF6E6B7B)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_tertiary = LifeOSTealBase
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = LifeOSTealContainerLight
val md_theme_light_onTertiaryContainer = LifeOSTealContainerDark
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = LifeOSStatusHighPriorityContainer
val md_theme_light_onErrorContainer = LifeOSStatusHighPriority
val md_theme_light_surface = LifeOSSurfaceLight
val md_theme_light_onSurface = LifeOSOnSurfaceLight
val md_theme_light_surfaceVariant = Color(0xFFECEAF3)
val md_theme_light_onSurfaceVariant = LifeOSOnSurfaceVariantLight
val md_theme_light_outline = LifeOSOutlineLight

// --- Dark scheme ---
val md_theme_dark_primary = Color(0xFFC4B5FD)
val md_theme_dark_onPrimary = Color(0xFF3B0F82)
val md_theme_dark_primaryContainer = LifeOSVioletContainerDark
val md_theme_dark_onPrimaryContainer = LifeOSVioletContainerLight
val md_theme_dark_secondary = Color(0xFFC9C5D4)
val md_theme_dark_onSecondary = Color(0xFF302E3D)
val md_theme_dark_tertiary = Color(0xFF5EEAD4)
val md_theme_dark_onTertiary = Color(0xFF00382F)
val md_theme_dark_tertiaryContainer = LifeOSTealContainerDark
val md_theme_dark_onTertiaryContainer = LifeOSTealContainerLight
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_surface = LifeOSSurfaceDark
val md_theme_dark_onSurface = LifeOSOnSurfaceDark
val md_theme_dark_surfaceVariant = Color(0xFF2B2836)
val md_theme_dark_onSurfaceVariant = LifeOSOnSurfaceVariantDark
val md_theme_dark_outline = LifeOSOutlineDark
