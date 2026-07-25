package com.lifeos.app.features.auth.presentation.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The active-page pill / inactive-dot indicator seen in onboarding-2.png.
 * Kept local to this feature rather than added to the shared Design System:
 * it's a one-off compositional detail for this specific pager, not (yet) a
 * pattern reused elsewhere — promote it to `core/designsystem/components` if
 * a second pager (e.g. an AI Assistant tutorial) needs the same look.
 */
@Composable
fun OnboardingPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs),
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp)

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(LifeOSPillShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingPagerIndicatorPreview() {
    LifeOSTheme {
        OnboardingPagerIndicator(pageCount = 3, currentPage = 1)
    }
}
