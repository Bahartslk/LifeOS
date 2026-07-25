package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lifeos.app.core.designsystem.theme.LifeOSElevation
import com.lifeos.app.core.designsystem.theme.LifeOSGradients
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * The standard neutral card: white/surface background, large rounded
 * corners, soft elevation — used everywhere in the Stitch designs for
 * grouped content (stat tiles, trip cards, task rows, settings rows).
 *
 * [contentPadding] defaults to the standard card inset but can be overridden
 * (e.g. `PaddingValues(0.dp)`) for content that needs to reach the card's
 * edges, such as a full-bleed cover photo at the top of the card followed by
 * normally-padded text below it (see Home's Upcoming Journey card).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(LifeOSSpacing.lg),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = LifeOSShapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = LifeOSElevation.level1),
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * The "hero" gradient card — the Home "Intelligent Hub" card (home.png),
 * the assistant's own chat bubbles (ai-assistent.png), and the "AI
 * Optimized Plan" badge surface (travel-list.png) all use this same violet
 * gradient fill with white content. Reach for this whenever a surface needs
 * to read as "premium / AI-powered", never a one-off gradient Brush.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier = modifier
                .clip(LifeOSShapes.large)
                .background(LifeOSGradients.primary)
                .then(clickableModifier)
                .padding(LifeOSSpacing.lg),
        ) {
            content()
        }
    }
}
