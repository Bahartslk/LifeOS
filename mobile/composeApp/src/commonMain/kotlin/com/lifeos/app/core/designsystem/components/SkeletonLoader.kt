package com.lifeos.app.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.lifeos.app.core.designsystem.theme.LifeOSMotion
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * A single shimmering placeholder block. Compose the shape of whatever
 * content is loading (a line, an avatar, a card) by passing [modifier] with
 * a size and clip, or use [SkeletonCard] / [SkeletonLine] below for the two
 * most common cases (a card row, a line of text).
 */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val shimmerOffset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(LifeOSMotion.DURATION_AMBIENT_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton-shimmer",
    )

    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface

    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(shimmerOffset * 300f, 0f),
        end = Offset(shimmerOffset * 300f + 300f, 0f),
    )

    Box(modifier = modifier.background(brush))
}

/** A shimmering line, sized for a single line of text — see [LifeOSSize.skeletonLineHeight]. */
@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    width: Dp? = null,
) {
    val sizedModifier = if (width != null) {
        modifier.width(width)
    } else {
        modifier.fillMaxWidth()
    }
    SkeletonBlock(
        modifier = sizedModifier
            .height(LifeOSSize.skeletonLineHeight)
            .clip(LifeOSShapes.extraSmall),
    )
}

/** A shimmering placeholder shaped like an [AppCard] — a title line and two body lines. */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
            SkeletonLine(modifier = Modifier.fillMaxWidth(0.6f))
            SkeletonLine()
            SkeletonLine(modifier = Modifier.fillMaxWidth(0.8f))
        }
    }
}

/**
 * A full-screen loading state: [count] [SkeletonCard]s in a scrollable
 * list. Home, Travel List, and Travel Detail each rendered this exact
 * `LazyColumn { items(count) { SkeletonCard(...) } }` shape independently,
 * differing only in [count] — factored out once that duplication reached
 * three call sites.
 */
@Composable
fun SkeletonListContent(count: Int, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(LifeOSSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
    ) {
        items(count) {
            SkeletonCard(modifier = Modifier.fillMaxSize())
        }
    }
}
