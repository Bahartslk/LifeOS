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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.DesignSystemStrings
import com.lifeos.app.core.designsystem.theme.LifeOSMotion
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * The default, generic loading spinner. Used by any screen awaiting a
 * network response, per docs/08-non-functional-requirements.md#performance.
 */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = DesignSystemStrings.LOADING },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * The distinctive branded "AI is working" indicator — the multi-segment
 * filling bar under "Generating Itinerary, Packing List, and Budget..." in
 * create-travel.png. [label] defaults to a generic Turkish placeholder;
 * pass the specific in-progress steps (e.g. "İtinerer, paket listesi ve
 * bütçe hazırlanıyor...") from the calling feature for real usage.
 */
@Composable
fun AiThinkingIndicator(
    modifier: Modifier = Modifier,
    label: String = DesignSystemStrings.AI_THINKING,
    segmentCount: Int = 3,
) {
    val transition = rememberInfiniteTransition(label = "ai-thinking")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = segmentCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = LifeOSMotion.DURATION_AMBIENT_MS * segmentCount,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ai-thinking-progress",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(LifeOSSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs),
        ) {
            repeat(segmentCount) { index ->
                val segmentFill = (progress - index).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(LifeOSPillShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(segmentFill)
                            .height(6.dp)
                            .clip(LifeOSPillShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}
