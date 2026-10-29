package com.lifeos.app.features.ai.presentation.chat.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.features.ai.presentation.chat.AiChatStrings
import com.lifeos.app.features.ai.presentation.chat.AiChatUiState

/**
 * The bottom of the chat: an error with "Tekrar dene" when the last message
 * failed, how many messages are left today, and the input with its
 * character counter and send button. Once today's limit is used up the input
 * is replaced by a short note.
 */
@Composable
fun ChatInputBar(
    state: AiChatUiState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = LifeOSSpacing.xs) {
        Column(
            modifier = Modifier.padding(horizontal = LifeOSSpacing.lg, vertical = LifeOSSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
        ) {
            if (state.isLimitReached) {
                Text(
                    text = AiChatStrings.LIMIT_REACHED_MESSAGE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = LifeOSSpacing.md)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
                return@Column
            }

            val error = state.errorMessage
            if (error != null) {
                // Stacked, not side by side: the outlined button fills the width,
                // which would squeeze a text next to it down to nothing.
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                )
                if (state.canRetry) {
                    AppOutlinedButton(text = AiChatStrings.RETRY_ACTION, onClick = onRetryClick)
                }
            }

            state.remainingMessages?.let { remaining ->
                Text(
                    text = AiChatStrings.remainingMessages(remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AppTextField(
                value = state.input,
                onValueChange = onInputChange,
                label = AiChatStrings.INPUT_LABEL,
                placeholder = AiChatStrings.INPUT_PLACEHOLDER,
                singleLine = false,
                enabled = !state.isSending,
                topEndContent = {
                    Text(
                        text = AiChatStrings.characterCount(state.input.length),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = AiChatStrings.characterCountDescription(state.input.length)
                        },
                    )
                },
                trailingContent = {
                    IconButton(onClick = onSendClick, enabled = state.canSend) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = AiChatStrings.SEND_DESCRIPTION,
                            tint = if (state.canSend) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                },
            )
        }
    }
}
