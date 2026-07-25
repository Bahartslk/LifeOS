package com.lifeos.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The Stitch designs use a bold, rounded, geometric display face for
 * headlines ("Welcome back", "Plan smarter, stay organized, and achieve
 * more.") — a distinct brand typeface, not the platform default.
 *
 * No font file is bundled with this bootstrap (font assets are binary and
 * outside the scope of this design system change); [FontFamily.Default] is
 * used as a placeholder so weight/size/spacing decisions below are correct
 * today and only the family needs to change in this one place once a brand
 * font (e.g. a rounded geometric sans such as Plus Jakarta Sans or Poppins)
 * is licensed and added as a font resource.
 */
val LifeOSFontFamily: FontFamily = FontFamily.Default

/**
 * Material 3 type scale, weighted and spaced to match docs/06-design-system.md#typography
 * and the Stitch designs: bold display headlines, regular body copy, and a
 * distinct wide-letter-spaced uppercase label style for "eyebrow" text
 * ("UPCOMING TRIP", "EXECUTIVE FUNCTION HUB", "READY TO LAUNCH") — see
 * [LifeOSTextStyles.overline] for the latter, since Material 3's scale has
 * no built-in overline role.
 */
val LifeOSTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

/**
 * Text styles the Material 3 [Typography] scale has no slot for, but that
 * recur throughout the Stitch designs. Referenced directly
 * (`LifeOSTextStyles.overline`), not through `MaterialTheme.typography`.
 */
object LifeOSTextStyles {
    /**
     * Uppercase, wide-tracking eyebrow/overline label — e.g. "UPCOMING TRIP"
     * (travel-list.png), "EXECUTIVE FUNCTION HUB" (onboarding-1.png),
     * "AI TRAVEL PLANNER" (create-travel.png). Always paired with
     * [SectionHeader] or [EyebrowChip], never used standalone for body copy.
     */
    val overline = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    )

    /**
     * Large bold numerals for stat displays — "18/24", "92%", "126"
     * (home.png Overview cards, profile.png stat tiles).
     */
    val statDisplay = TextStyle(
        fontFamily = LifeOSFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
    )
}
