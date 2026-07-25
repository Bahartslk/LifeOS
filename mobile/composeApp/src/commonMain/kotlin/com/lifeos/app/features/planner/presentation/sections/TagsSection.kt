package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSPillShape
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Tags" (Create Task's requirement) — a text field to add one tag at a
 * time plus the already-added tags as removable chips below, reusing
 * [AppTextField] and the same `FlowRow`-of-pills shape [TaskInfoCard]'s
 * `TagsRow` established for Task Detail's read-only display. [RemovableTagChip]
 * stays private/feature-local rather than a [com.lifeos.app.core.designsystem.components.StatusChip]
 * variant — no other feature needs a removable chip yet.
 */
@Composable
fun TagsSection(
    tags: List<String>,
    tagInput: String,
    onTagInputChanged: (String) -> Unit,
    onTagAdded: () -> Unit,
    onTagRemoved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = tagInput,
            onValueChange = onTagInputChanged,
            label = PlannerStrings.TAGS_LABEL,
            placeholder = PlannerStrings.TAGS_INPUT_PLACEHOLDER,
            trailingContent = {
                IconButton(onClick = onTagAdded) {
                    AppIcon(imageVector = Icons.Filled.Add, contentDescription = PlannerStrings.TAGS_ADD_CONTENT_DESCRIPTION)
                }
            },
        )
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(LifeOSSpacing.md))
            TagsChipsRow(tags = tags, onTagRemoved = onTagRemoved)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsChipsRow(tags: List<String>, onTagRemoved: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
        tags.forEach { tag -> RemovableTagChip(tag = tag, onRemoveClick = { onTagRemoved(tag) }) }
    }
}

@Composable
private fun RemovableTagChip(tag: String, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(LifeOSPillShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = LifeOSSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(LifeOSSpacing.xs))
        AppIcon(
            imageVector = Icons.Filled.Close,
            contentDescription = PlannerStrings.TAGS_REMOVE_CONTENT_DESCRIPTION,
            size = TAG_REMOVE_ICON_SIZE,
            modifier = Modifier
                .clickable(onClick = onRemoveClick)
                .padding(LifeOSSpacing.xs),
        )
    }
}

private val TAG_REMOVE_ICON_SIZE = 14.dp

@Preview
@Composable
private fun TagsSectionEmptyPreview() {
    LifeOSTheme {
        TagsSection(tags = emptyList(), tagInput = "", onTagInputChanged = {}, onTagAdded = {}, onTagRemoved = {})
    }
}

@Preview
@Composable
private fun TagsSectionWithTagsPreview() {
    LifeOSTheme {
        TagsSection(
            tags = listOf("Ayşe", "Mehmet", "Zeynep"),
            tagInput = "",
            onTagInputChanged = {},
            onTagAdded = {},
            onTagRemoved = {},
        )
    }
}
