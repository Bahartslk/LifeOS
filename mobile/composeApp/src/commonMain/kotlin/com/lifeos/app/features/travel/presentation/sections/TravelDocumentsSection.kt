package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase
import com.lifeos.app.core.designsystem.theme.LifeOSTealContainerLight
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.DocumentStatus
import com.lifeos.app.features.travel.domain.model.DocumentType
import com.lifeos.app.features.travel.domain.model.TravelDocument
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Travel Documents" (this task's requirement — passport, visa, insurance,
 * boarding pass; not pictured in travel-details.png itself). Reuses
 * [AppCard] + [StatusChip] with a status-driven tint.
 */
@Composable
fun TravelDocumentsSection(
    documents: List<TravelDocument>,
    onDocumentClick: (DocumentType) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.DOCUMENTS_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
            documents.forEach { document ->
                DocumentRow(document = document, onClick = { onDocumentClick(document.type) })
            }
        }
    }
}

@Composable
private fun DocumentRow(document: TravelDocument, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(imageVector = document.type.toIcon(), contentDescription = null)
            Spacer(modifier = Modifier.width(LifeOSSpacing.md))
            Text(text = document.type.label(), style = MaterialTheme.typography.bodyLarge)
        }
        val (containerColor, contentColor) = document.status.colors()
        StatusChip(label = document.status.label(), containerColor = containerColor, contentColor = contentColor)
    }
}

private fun DocumentType.toIcon(): ImageVector = when (this) {
    DocumentType.PASSPORT -> Icons.Filled.Portrait
    DocumentType.VISA -> Icons.Filled.Badge
    DocumentType.INSURANCE -> Icons.Filled.HealthAndSafety
    DocumentType.BOARDING_PASS -> Icons.Filled.ConfirmationNumber
}

private fun DocumentType.label(): String = when (this) {
    DocumentType.PASSPORT -> TravelStrings.DOCUMENT_PASSPORT
    DocumentType.VISA -> TravelStrings.DOCUMENT_VISA
    DocumentType.INSURANCE -> TravelStrings.DOCUMENT_INSURANCE
    DocumentType.BOARDING_PASS -> TravelStrings.DOCUMENT_BOARDING_PASS
}

private fun DocumentStatus.label(): String = when (this) {
    DocumentStatus.READY -> TravelStrings.DOCUMENT_STATUS_READY
    DocumentStatus.PENDING -> TravelStrings.DOCUMENT_STATUS_PENDING
    DocumentStatus.MISSING -> TravelStrings.DOCUMENT_STATUS_MISSING
}

@Composable
private fun DocumentStatus.colors(): Pair<Color, Color> =
    when (this) {
        DocumentStatus.READY -> LifeOSTealContainerLight to LifeOSTealBase
        DocumentStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DocumentStatus.MISSING -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
    }

@Preview
@Composable
private fun TravelDocumentsSectionPreview() {
    LifeOSTheme {
        TravelDocumentsSection(
            documents = listOf(
                TravelDocument(DocumentType.PASSPORT, DocumentStatus.READY),
                TravelDocument(DocumentType.VISA, DocumentStatus.READY),
                TravelDocument(DocumentType.INSURANCE, DocumentStatus.PENDING),
                TravelDocument(DocumentType.BOARDING_PASS, DocumentStatus.MISSING),
            ),
            onDocumentClick = {},
        )
    }
}
