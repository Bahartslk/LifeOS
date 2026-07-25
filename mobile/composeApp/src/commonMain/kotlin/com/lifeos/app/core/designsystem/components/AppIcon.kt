package com.lifeos.app.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.app.core.designsystem.theme.LifeOSSize

/**
 * Thin wrapper around Material 3's [Icon] that standardizes size to the
 * [LifeOSSize] scale, per docs/06-design-system.md#iconography ("Icons are
 * used consistently per meaning across modules").
 *
 * [contentDescription] has no default — every call site must consciously
 * decide between a real description (interactive/meaningful icon) or an
 * explicit `null` (purely decorative icon next to visible text), rather
 * than silently omitting accessibility information.
 */
@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: androidx.compose.ui.unit.Dp = LifeOSSize.iconMedium,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}
