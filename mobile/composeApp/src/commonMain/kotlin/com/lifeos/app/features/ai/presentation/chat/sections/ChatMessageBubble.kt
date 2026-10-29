package com.lifeos.app.features.ai.presentation.chat.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.features.ai.domain.model.ChatRole
import com.lifeos.app.features.ai.presentation.chat.AiChatStrings
import com.lifeos.app.features.ai.presentation.chat.ChatMessageStatus
import com.lifeos.app.features.ai.presentation.chat.ChatMessageUi

/**
 * One chat message: the user's on the right in the primary color, the
 * assistant's on the left on a neutral surface. Both come from the theme, so
 * they adapt to dark mode. Screen readers hear who said it ("Sen: …",
 * "Asistan: …"), plus the failure note when a message couldn't be sent.
 */
@Composable
fun ChatMessageBubble(message: ChatMessageUi, modifier: Modifier = Modifier) {
    val isUser = message.role == ChatRole.USER
    val failed = message.status == ChatMessageStatus.FAILED
    val description = buildString {
        append(
            if (isUser) {
                AiChatStrings.userMessageDescription(message.text)
            } else {
                AiChatStrings.assistantMessageDescription(message.text)
            },
        )
        if (failed) append(". ${AiChatStrings.FAILED_LABEL}")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = MAX_BUBBLE_WIDTH).clearAndSetSemantics { contentDescription = description },
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = BUBBLE_RADIUS,
                    topEnd = BUBBLE_RADIUS,
                    bottomStart = if (isUser) BUBBLE_RADIUS else TAIL_RADIUS,
                    bottomEnd = if (isUser) TAIL_RADIUS else BUBBLE_RADIUS,
                ),
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = LifeOSSpacing.md, vertical = LifeOSSpacing.sm),
                )
            }
            if (failed) {
                Text(
                    text = AiChatStrings.FAILED_LABEL,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = LifeOSSpacing.xs),
                )
            }
        }
    }
}

private val MAX_BUBBLE_WIDTH = 300.dp
private val BUBBLE_RADIUS = 18.dp
private val TAIL_RADIUS = 4.dp
