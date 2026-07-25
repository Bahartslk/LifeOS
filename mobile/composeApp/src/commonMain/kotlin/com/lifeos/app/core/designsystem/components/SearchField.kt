package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.DesignSystemStrings
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape

/**
 * A filled, fully-rounded search input — the same pill treatment as the AI
 * Assistant chat input in ai-assistent.png, reused for in-app search (e.g.
 * searching Trips or Tasks). Defaults to a Turkish placeholder ("Ara") but
 * accepts an override for context-specific prompts.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = DesignSystemStrings.SEARCH_PLACEHOLDER,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            AppIcon(imageVector = Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    AppIcon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = DesignSystemStrings.SEARCH_CLEAR,
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = LifeOSPillShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
