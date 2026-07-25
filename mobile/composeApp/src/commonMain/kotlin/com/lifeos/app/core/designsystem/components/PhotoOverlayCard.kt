package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * A cover-photo card with a bottom scrim and overlaid content — the
 * "Kyoto, Japan" / "Paris, France" trip cards (travel-list.png) and the
 * Home "Your Next Adventure" card (home.png) all use this exact shape:
 * full-bleed image, optional badges pinned to the top corners, and a
 * gradient-scrimmed text block anchored to the bottom for legibility over
 * the photo.
 *
 * Extracted here after the second feature (Travel List) needed the same
 * pattern Home's Upcoming Journey card had already hand-rolled — see
 * `UpcomingJourneySection.kt`'s `JourneyCoverImage`, now built on this.
 *
 * Renders no explicit `clip`: intended to be the first/top child of an
 * [AppCard] with `contentPadding = PaddingValues(0.dp)`, which already
 * clips all of its content to the card's rounded shape.
 */
@Composable
fun PhotoOverlayCard(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    height: Dp = LifeOSSize.cardImageHeightLarge,
    topStartContent: (@Composable () -> Unit)? = null,
    topEndContent: (@Composable () -> Unit)? = null,
    bottomContent: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                    ),
                ),
        )
        if (topStartContent != null) {
            Box(modifier = Modifier.align(Alignment.TopStart).padding(LifeOSSpacing.md)) {
                topStartContent()
            }
        }
        if (topEndContent != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(LifeOSSpacing.md)) {
                topEndContent()
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(LifeOSSpacing.lg),
            content = bottomContent,
        )
    }
}
