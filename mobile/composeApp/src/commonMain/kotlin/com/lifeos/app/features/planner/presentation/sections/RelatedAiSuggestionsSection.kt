package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppSecondaryButton
import com.lifeos.app.core.designsystem.components.GradientCard
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Related AI Suggestions (placeholder)" (this task's requirement) — a
 * teaser for the future AI Assistant integration, reusing the exact
 * icon-bubble-plus-message-plus-button [GradientCard] shape
 * [ProgressSection] (Planner Dashboard's AI-insight card) already
 * established for "AI-authored" surfaces, so this reads as the same
 * feature family rather than a new visual concept.
 */
@Composable
fun RelatedAiSuggestionsSection(
    onViewSuggestionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GradientCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(LifeOSSize.avatarSmall)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = ICON_BACKGROUND_ALPHA)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    size = LifeOSSize.iconSmall,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = PlannerStrings.AI_SUGGESTIONS_TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
                Text(
                    text = PlannerStrings.AI_SUGGESTIONS_DESCRIPTION,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppSecondaryButton(
            text = PlannerStrings.AI_SUGGESTIONS_ACTION,
            onClick = onViewSuggestionsClick,
            onGradient = true,
        )
    }
}

private const val ICON_BACKGROUND_ALPHA = 0.15f

@Preview
@Composable
private fun RelatedAiSuggestionsSectionPreview() {
    LifeOSTheme {
        RelatedAiSuggestionsSection(onViewSuggestionsClick = {})
    }
}
