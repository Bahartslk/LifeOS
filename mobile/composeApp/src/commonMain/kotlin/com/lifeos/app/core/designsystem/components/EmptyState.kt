package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSShapes
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * Informative (never generic) empty state, per
 * docs/08-non-functional-requirements.md#usability — e.g. "Henüz seyahatiniz
 * yok — ilk seyahatinizi oluşturun" rather than a blank screen. Used by My
 * Trips, Task List, and AI Conversation History when no data exists yet.
 *
 * [bordered] reproduces the dashed-outline card style of "Past Archives" in
 * travel-list.png — appropriate for an empty state embedded inline at the
 * end of a list, as opposed to a full-screen empty state (leave `false`).
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    bordered: Boolean = false,
    action: @Composable (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LifeOSSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(LifeOSSize.avatarMedium)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            action?.invoke()
        }
    }

    if (bordered) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .dashedBorder(color = MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * A dashed rounded-rect outline — Compose has no built-in equivalent of
 * Android's dashed `<shape>` drawable — reproducing the "Past Archives"
 * card border in travel-list.png.
 */
private fun Modifier.dashedBorder(color: Color): Modifier =
    this.clip(LifeOSShapes.large).drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
            style = DrawStroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
            ),
        )
    }
