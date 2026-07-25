package com.lifeos.app.features.ai.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.ai.presentation.AiAssistantStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The screen header (ai-assistent.png: "🤖 AI Asistan" + "Bugün sana nasıl
 * yardımcı olabilirim?"). Deliberately not a full conversational greeting —
 * this integration builds the structured context dashboard the mockup's
 * future chat experience will eventually sit behind, not the chat UI
 * itself (see this feature's KDoc on `AiAssistantViewModel`).
 */
@Composable
fun AiAssistantHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = LifeOSSpacing.lg)) {
        AppIcon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(text = AiAssistantStrings.HEADER_TITLE, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        Text(
            text = AiAssistantStrings.HEADER_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun AiAssistantHeaderPreview() {
    LifeOSTheme {
        AiAssistantHeader()
    }
}
