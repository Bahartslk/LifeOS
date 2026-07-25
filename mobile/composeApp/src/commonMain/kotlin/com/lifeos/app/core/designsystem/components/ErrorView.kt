package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.DesignSystemStrings
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing

/**
 * Shared error state — a recoverable failure (network error, AI provider
 * timeout, per docs/09-ai-features.md#limitations) with a retry action.
 * Defaults to generic Turkish copy; pass [title]/[description] for a
 * context-specific message where one is available.
 */
@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    title: String = DesignSystemStrings.ERROR_TITLE,
    description: String? = null,
    retryLabel: String = DesignSystemStrings.ERROR_RETRY,
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(LifeOSSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm),
        ) {
            AppIcon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                size = LifeOSSize.iconLarge,
            )
            Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (onRetry != null) {
                AppOutlinedButton(
                    text = retryLabel,
                    onClick = onRetry,
                    modifier = Modifier.padding(top = LifeOSSpacing.sm),
                )
            }
        }
    }
}
