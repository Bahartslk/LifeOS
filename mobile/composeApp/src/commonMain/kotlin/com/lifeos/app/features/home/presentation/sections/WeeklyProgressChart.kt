package com.lifeos.app.features.home.presentation.sections

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A minimal 7-bar chart for the Overview section's weekly completion
 * ratios. Deliberately a small local `Canvas` composable rather than a
 * charting library dependency — this is the only chart LifeOS needs today,
 * and one is easy to draw directly; add a real charting dependency only
 * once a second, more complex chart is actually needed elsewhere.
 *
 * [ratios] must have exactly 7 entries (Monday..Sunday), each in `0f..1f`.
 */
@Composable
fun WeeklyProgressChart(
    ratios: List<Float>,
    modifier: Modifier = Modifier,
) {
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(WEEKLY_CHART_HEIGHT),
    ) {
        drawWeeklyBars(ratios = ratios, barColor = barColor, trackColor = trackColor)
    }
}

private fun DrawScope.drawWeeklyBars(ratios: List<Float>, barColor: Color, trackColor: Color) {
    val barCount = ratios.size
    val gap = BAR_GAP.toPx()
    val totalGapWidth = gap * (barCount - 1)
    val barWidth = (size.width - totalGapWidth) / barCount
    val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

    ratios.forEachIndexed { index, ratio ->
        val left = index * (barWidth + gap)
        val clampedRatio = ratio.coerceIn(0f, 1f)
        val barHeight = size.height * clampedRatio

        drawRoundRect(
            color = trackColor,
            topLeft = Offset(left, 0f),
            size = Size(barWidth, size.height),
            cornerRadius = cornerRadius,
        )
        drawRoundRect(
            color = barColor,
            topLeft = Offset(left, size.height - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = cornerRadius,
        )
    }
}

private val WEEKLY_CHART_HEIGHT = 64.dp
private val BAR_GAP = LifeOSSpacing.xs

@Preview
@Composable
private fun WeeklyProgressChartPreview() {
    LifeOSTheme {
        WeeklyProgressChart(ratios = listOf(0.6f, 0.8f, 0.5f, 0.9f, 0.7f, 0.85f, 0.75f))
    }
}
