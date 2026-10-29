package com.lifeos.app.features.ai.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.SuggestionChip
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.ai.domain.model.ChatRole
import com.lifeos.app.features.ai.presentation.chat.sections.ChatInputBar
import com.lifeos.app.features.ai.presentation.chat.sections.ChatMessageBubble
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/** The chat screen's route: collects [AiChatViewModel]; back is plain navigation. */
@Composable
fun AiChatRoute(
    onNavigateBack: () -> Unit,
    viewModel: AiChatViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    AiChatScreen(uiState = uiState, onEvent = viewModel::onEvent, onNavigateBack = onNavigateBack)
}

@Composable
private fun AiChatScreen(
    uiState: AiChatUiState,
    onEvent: (AiChatEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = AiChatStrings.TITLE,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = AiChatStrings.BACK_DESCRIPTION,
                onNavigationClick = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()) {
            if (uiState.showSuggestions) {
                ChatSuggestions(
                    onSuggestionClick = { onEvent(AiChatEvent.SuggestionClicked(it)) },
                    modifier = Modifier.weight(1f),
                )
            } else {
                ChatMessageList(uiState = uiState, modifier = Modifier.weight(1f))
            }
            ChatInputBar(
                state = uiState,
                onInputChange = { onEvent(AiChatEvent.InputChanged(it)) },
                onSendClick = { onEvent(AiChatEvent.SendClicked) },
                onRetryClick = { onEvent(AiChatEvent.RetryClicked) },
            )
        }
    }
}

@Composable
private fun ChatMessageList(uiState: AiChatUiState, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val itemCount = uiState.messages.size + if (uiState.isSending) 1 else 0

    // Keep the newest message (or the typing note) in view.
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = LifeOSSpacing.lg, vertical = LifeOSSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
    ) {
        items(uiState.messages, key = { it.id }) { message -> ChatMessageBubble(message) }
        if (uiState.isSending) {
            item(key = "typing") {
                Text(
                    text = AiChatStrings.TYPING,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatSuggestions(onSuggestionClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(LifeOSSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
    ) {
        EmptyState(
            title = AiChatStrings.EMPTY_TITLE,
            description = AiChatStrings.EMPTY_DESCRIPTION,
            icon = Icons.Filled.AutoAwesome,
        )
        Text(
            text = AiChatStrings.SUGGESTIONS_TITLE,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
        ) {
            AiChatStrings.SUGGESTIONS.forEach { suggestion ->
                SuggestionChip(
                    label = suggestion,
                    onClick = { onSuggestionClick(suggestion) },
                    modifier = Modifier.semantics {
                        contentDescription = AiChatStrings.suggestionDescription(suggestion)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun AiChatScreenEmptyPreview() {
    LifeOSTheme { AiChatScreen(uiState = AiChatUiState(), onEvent = {}, onNavigateBack = {}) }
}

@Preview
@Composable
private fun AiChatScreenConversationPreview() {
    LifeOSTheme {
        AiChatScreen(
            uiState = AiChatUiState(
                messages = listOf(
                    ChatMessageUi(0, ChatRole.USER, "Bugün neye odaklanmalıyım?"),
                    ChatMessageUi(1, ChatRole.ASSISTANT, "Önce saat 16:00'daki raporu bitirmeye odaklanabilirsin."),
                ),
                remainingMessages = 4,
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
