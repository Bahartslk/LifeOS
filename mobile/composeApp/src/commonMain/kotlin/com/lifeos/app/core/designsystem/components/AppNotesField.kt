package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A labeled, multi-line free-text field — Travel Detail's and Task Detail's
 * personal-notes fields both need exactly this shape. Reuses [AppTextField]
 * with its `minLines` parameter rather than a new text-area component.
 * Originally feature-local to Travel (`NotesSection`); promoted here once
 * Planner needed the identical field, per this project's "only extract a
 * Design System component once it's reused by at least two features" rule.
 *
 * [label]/[placeholder] have no default — unlike a Design System component
 * with one obvious use (e.g. [SearchField]), "notes" copy is genuinely
 * feature-specific ("Notlarım" for a trip vs. "Notlar" for a task), so
 * every caller supplies its own rather than silently inheriting Travel's.
 */
@Composable
fun AppNotesField(
    notes: String,
    onNotesChanged: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = notes,
        onValueChange = onNotesChanged,
        label = label,
        placeholder = placeholder,
        modifier = modifier.fillMaxWidth(),
        singleLine = false,
        minLines = NOTES_MIN_LINES,
    )
}

private const val NOTES_MIN_LINES = 4
