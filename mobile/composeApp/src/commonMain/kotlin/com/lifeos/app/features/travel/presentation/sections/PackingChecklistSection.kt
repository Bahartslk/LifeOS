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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.PackingCategory
import com.lifeos.app.features.travel.domain.model.PackingItem
import com.lifeos.app.features.travel.domain.model.PackingItemIcon
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Packing" (travel-details.png: "Camera gear" / "Warm jacket" / "Hiking
 * boots", "3 of 8"). Genuinely interactive — [onItemToggled] flips real
 * checked/unchecked state in [TravelDetailViewModel][com.lifeos.app.features.travel.presentation.TravelDetailViewModel],
 * unlike Travel List's "UI only" search/filter.
 */
@Composable
fun PackingChecklistSection(
    categories: List<PackingCategory>,
    onItemToggled: (String) -> Unit,
    onViewFullListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allItems = categories.flatMap { it.items }
    val checkedCount = allItems.count { it.isChecked }

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = TravelStrings.PACKING_TITLE, style = MaterialTheme.typography.titleLarge)
            StatusChip(label = TravelStrings.packingProgressLabel(checkedCount, allItems.size))
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xs)) {
            allItems.forEach { item -> PackingItemRow(item = item, onToggle = { onItemToggled(item.id) }) }
        }

        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppOutlinedButton(text = TravelStrings.PACKING_VIEW_FULL_LIST, onClick = onViewFullListClick)
    }
}

@Composable
private fun PackingItemRow(item: PackingItem, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
            color = if (item.isChecked) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(LifeOSSpacing.sm))
        AppIcon(
            imageVector = item.icon.toImageVector(),
            contentDescription = null,
            size = LifeOSSize.iconSmall,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun PackingItemIcon.toImageVector(): ImageVector = when (this) {
    PackingItemIcon.CAMERA -> Icons.Filled.Camera
    PackingItemIcon.COLD_WEATHER -> Icons.Filled.Checkroom
    PackingItemIcon.FOOTWEAR -> Icons.Filled.DirectionsWalk
    PackingItemIcon.DOCUMENTS -> Icons.Filled.Description
    PackingItemIcon.ELECTRONICS -> Icons.Filled.Memory
    PackingItemIcon.TOILETRIES -> Icons.Filled.Face
    PackingItemIcon.GENERAL -> Icons.Filled.Inventory2
}

@Preview
@Composable
private fun PackingChecklistSectionPreview() {
    LifeOSTheme {
        PackingChecklistSection(
            categories = listOf(
                PackingCategory(
                    name = "Genel",
                    items = listOf(
                        PackingItem("1", "Kamera ekipmanı", true, PackingItemIcon.CAMERA),
                        PackingItem("2", "Kalın mont", true, PackingItemIcon.COLD_WEATHER),
                        PackingItem("3", "Yürüyüş botları", true, PackingItemIcon.FOOTWEAR),
                        PackingItem("4", "Powerbank", false, PackingItemIcon.ELECTRONICS),
                    ),
                ),
            ),
            onItemToggled = {},
            onViewFullListClick = {},
        )
    }
}
