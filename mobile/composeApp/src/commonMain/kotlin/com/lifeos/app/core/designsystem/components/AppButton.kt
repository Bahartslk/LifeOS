package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSGradients
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase
import com.lifeos.app.core.designsystem.theme.lifeOSGlow

/**
 * Buttons in three flavors, matching the exact patterns used across the
 * Stitch designs. All three are full-width pills at [LifeOSSize.buttonHeight]
 * by default, per docs/06-design-system.md#components ("Consistent
 * component sizes") — pass a narrower [Modifier] to override.
 */

/**
 * The single primary call-to-action per screen: a solid violet-to-purple
 * gradient pill with a soft matching glow and white text, per login.png
 * ("Login"), onboarding-2.png ("Next Step"), onboarding-3.png ("Get Started").
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LifeOSSize.buttonHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .lifeOSGlow(shape = LifeOSPillShape)
            .clip(LifeOSPillShape)
            .background(LifeOSGradients.primary)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled && !loading,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(
            text = text,
            contentColor = Color.White,
            loading = loading,
            trailingIcon = trailingIcon,
        )
    }
}

/**
 * A secondary action. On a light/neutral surface this is a light-violet
 * tonal pill (violet text on a pale violet fill). Set [onGradient] to `true`
 * when placing it on top of a [LifeOSGradients] surface — e.g. the white
 * "Complete Checklist" pill on the Home "Intelligent Hub" card (home.png) —
 * which flips it to a solid white fill with violet text for contrast.
 */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onGradient: Boolean = false,
    trailingIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val containerColor = if (onGradient) Color.White else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (onGradient) LifeOSVioletBase else MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LifeOSSize.buttonHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(LifeOSPillShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(text = text, contentColor = contentColor, trailingIcon = trailingIcon)
    }
}

/**
 * A bordered, transparent-background button — "Explore Features" and
 * "Continue with Google" on onboarding-3.png / login.png. Set [onGradient]
 * to `true` for the translucent-white-border variant used on top of a
 * [LifeOSGradients] surface (e.g. "View Trip" on the Home hero card).
 */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onGradient: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (onGradient) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (onGradient) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LifeOSSize.buttonHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(LifeOSPillShape)
            .border(BorderStroke(1.dp, borderColor), LifeOSPillShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(
            text = text,
            contentColor = contentColor,
            leadingIcon = leadingIcon,
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier.padding(horizontal = LifeOSSpacing.xl),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
            return@Row
        }
        if (leadingIcon != null) {
            AppIcon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                size = LifeOSSize.iconSmall,
                modifier = Modifier.padding(end = LifeOSSpacing.sm),
            )
        }
        Text(text = text, color = contentColor, style = MaterialTheme.typography.labelLarge)
        if (trailingIcon != null) {
            AppIcon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = contentColor,
                size = LifeOSSize.iconSmall,
                modifier = Modifier.padding(start = LifeOSSpacing.sm),
            )
        }
    }
}
