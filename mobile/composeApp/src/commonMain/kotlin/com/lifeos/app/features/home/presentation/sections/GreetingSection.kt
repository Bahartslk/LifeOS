package com.lifeos.app.features.home.presentation.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppAvatar
import com.lifeos.app.core.designsystem.components.AppGreetingTopBar
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.home.domain.model.GreetingInfo
import com.lifeos.app.features.home.presentation.HomeStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The personalized header (home.png: avatar, "Good morning, Bahar 👋",
 * AI-summary subtitle, settings shortcut). A thin composition of two
 * existing Design System pieces ([AppAvatar], [AppGreetingTopBar]) — no new
 * component was needed here beyond extending [AppGreetingTopBar] with a
 * leading-content slot.
 *
 * Takes the whole [GreetingInfo] rather than its two fields unpacked,
 * matching every sibling section's "one domain object in" shape
 * ([OverviewSection] takes `OverviewStats`, [UpcomingJourneySection] takes
 * `UpcomingJourney`, etc.). [greetingMessage] stays a separate parameter —
 * it's the current time-of-day word ("Günaydın"/"İyi günler"), computed in
 * [com.lifeos.app.features.home.presentation.HomeViewModel] from the device
 * clock, not part of the fetched [GreetingInfo] data.
 */
@Composable
fun GreetingSection(
    greetingMessage: String,
    greeting: GreetingInfo,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppGreetingTopBar(
        greeting = "$greetingMessage, ${greeting.userFirstName}",
        subtitle = HomeStrings.GREETING_SUBTITLE,
        modifier = modifier,
        leadingContent = {
            AppAvatar(
                imageUrl = greeting.avatarUrl,
                contentDescription = HomeStrings.AVATAR_CONTENT_DESCRIPTION,
                size = LifeOSSize.avatarSmall,
            )
        },
        trailingIcon = Icons.Filled.Settings,
        trailingIconContentDescription = HomeStrings.SETTINGS_CONTENT_DESCRIPTION,
        onTrailingClick = onSettingsClick,
    )
}

@Preview
@Composable
private fun GreetingSectionPreview() {
    LifeOSTheme {
        GreetingSection(
            greetingMessage = HomeStrings.GREETING_MORNING,
            greeting = GreetingInfo(userFirstName = "Bahar", avatarUrl = null),
            onSettingsClick = {},
        )
    }
}
