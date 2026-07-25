package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.TaskAttachment
import com.lifeos.app.features.planner.presentation.PlannerStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Attachments (UI only)" (this task's requirement): a static file list —
 * no real file picker or storage exists, per this task's explicit scope.
 * [onAddAttachmentClick] surfaces a "coming soon" message, the same
 * graceful-degradation pattern every other not-yet-implemented action in
 * this app uses.
 */
@Composable
fun AttachmentSection(
    attachments: List<TaskAttachment>,
    onAddAttachmentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.ATTACHMENTS_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        if (attachments.isEmpty()) {
            Text(
                text = PlannerStrings.ATTACHMENTS_EMPTY,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                attachments.forEach { attachment ->
                    key(attachment.id) { AttachmentRow(attachment = attachment) }
                }
            }
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppOutlinedButton(
            text = PlannerStrings.ADD_ATTACHMENT_ACTION,
            onClick = onAddAttachmentClick,
            leadingIcon = Icons.Filled.AttachFile,
        )
    }
}

@Composable
private fun AttachmentRow(attachment: TaskAttachment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIcon(
            imageVector = Icons.Filled.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(LifeOSSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = attachment.fileName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = attachment.fileSizeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun AttachmentSectionPreview() {
    LifeOSTheme {
        AttachmentSection(
            attachments = listOf(
                TaskAttachment(id = "attachment-1", fileName = "Q3_Rapor_Taslak.pdf", fileSizeLabel = "2.4 MB"),
                TaskAttachment(id = "attachment-2", fileName = "Sunum_Notlari.docx", fileSizeLabel = "540 KB"),
            ),
            onAddAttachmentClick = {},
        )
    }
}

@Preview
@Composable
private fun AttachmentSectionEmptyPreview() {
    LifeOSTheme {
        AttachmentSection(attachments = emptyList(), onAddAttachmentClick = {})
    }
}
