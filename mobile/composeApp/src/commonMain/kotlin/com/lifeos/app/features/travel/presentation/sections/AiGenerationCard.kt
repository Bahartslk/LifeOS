package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.lifeos.app.core.designsystem.components.AiThinkingIndicator
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "AI Generation Section" (this task's screen content): the
 * "AI Seyahat Planı Oluştur" trigger, plus [AiThinkingIndicator] while
 * [isGenerating]. Reuses the same icon-bubble + violet-title language
 * [AiSummaryCard] already established for "this content came from AI",
 * rather than inventing a new visual treatment.
 */
@Composable
fun AiGenerationCard(
    isGenerating: Boolean,
    onGenerateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(LifeOSSize.avatarSmall)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = LifeOSVioletBase,
                    size = LifeOSSize.iconSmall,
                )
            }
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Text(
                text = TravelStrings.AI_GENERATION_CARD_TITLE,
                style = MaterialTheme.typography.titleMedium,
                color = LifeOSVioletBase,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Text(
            text = TravelStrings.AI_GENERATION_CARD_DESCRIPTION,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        if (isGenerating) {
            AiThinkingIndicator(label = TravelStrings.GENERATING_LABEL)
        } else {
            AppPrimaryButton(
                text = TravelStrings.GENERATE_TRIP_BUTTON,
                onClick = onGenerateClicked,
                trailingIcon = Icons.Filled.AutoAwesome,
            )
        }
    }
}

@Preview
@Composable
private fun AiGenerationCardIdlePreview() {
    LifeOSTheme {
        AiGenerationCard(isGenerating = false, onGenerateClicked = {})
    }
}

@Preview
@Composable
private fun AiGenerationCardGeneratingPreview() {
    LifeOSTheme {
        AiGenerationCard(isGenerating = true, onGenerateClicked = {})
    }
}
