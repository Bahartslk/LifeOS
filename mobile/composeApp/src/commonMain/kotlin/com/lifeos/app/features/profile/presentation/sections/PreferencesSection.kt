package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.OptionChipRow
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Preferences" (this sprint's requirement: Appearance, Language, AI
 * Preferences, Travel Preferences). [themeMode]/[onThemeModeSelected] are
 * the one genuinely working control on this screen — reused directly from
 * [com.lifeos.app.core.storage.ThemeMode] (this sprint's real, persisted
 * feature: [com.lifeos.app.core.storage.ThemePreferenceStorage] existed
 * since the mobile bootstrap specifically waiting for this screen to call
 * it). [OptionChipRow] is used instead of a simple on/off switch — a
 * binary switch can't represent [ThemeMode.SYSTEM] as a genuine third
 * state, so this deliberately doesn't pixel-match profile.png's simpler
 * two-state toggle (see this sprint's design-decisions note). Language, AI
 * Preferences, and Travel Preferences remain placeholder rows.
 */
@Composable
fun PreferencesSection(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageClick: () -> Unit,
    onAiPreferencesClick: () -> Unit,
    onTravelPreferencesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = ProfileStrings.PREFERENCES_TITLE)
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(LifeOSSpacing.md))
                    Text(text = ProfileStrings.APPEARANCE_LABEL, style = MaterialTheme.typography.bodyLarge)
                }
                OptionChipRow(
                    options = ThemeMode.entries,
                    selectedOption = themeMode,
                    labelFor = { mode -> mode.toLabel() },
                    onOptionSelected = onThemeModeSelected,
                )
            }
            Spacer(modifier = Modifier.height(LifeOSSpacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
                ProfileSettingsRow(
                    icon = Icons.Filled.Language,
                    label = ProfileStrings.LANGUAGE_ACTION,
                    trailingValue = ProfileStrings.LANGUAGE_VALUE,
                    onClick = onLanguageClick,
                )
                ProfileSettingsRow(
                    icon = Icons.Filled.AutoAwesome,
                    label = ProfileStrings.AI_PREFERENCES_ACTION,
                    onClick = onAiPreferencesClick,
                )
                ProfileSettingsRow(
                    icon = Icons.Filled.FlightTakeoff,
                    label = ProfileStrings.TRAVEL_PREFERENCES_ACTION,
                    onClick = onTravelPreferencesClick,
                )
            }
        }
    }
}

private fun ThemeMode.toLabel(): String = when (this) {
    ThemeMode.SYSTEM -> ProfileStrings.APPEARANCE_SYSTEM
    ThemeMode.LIGHT -> ProfileStrings.APPEARANCE_LIGHT
    ThemeMode.DARK -> ProfileStrings.APPEARANCE_DARK
}

@Preview
@Composable
private fun PreferencesSectionPreview() {
    LifeOSTheme {
        PreferencesSection(
            themeMode = ThemeMode.SYSTEM,
            onThemeModeSelected = {},
            onLanguageClick = {},
            onAiPreferencesClick = {},
            onTravelPreferencesClick = {},
        )
    }
}
