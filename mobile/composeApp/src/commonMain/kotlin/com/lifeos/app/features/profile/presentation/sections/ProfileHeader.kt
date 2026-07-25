package com.lifeos.app.features.profile.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppAvatar
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "User Information" (this sprint's requirement): avatar, name, email, and
 * the one "Edit Profile" entry point for the whole screen — Account's own
 * "Edit Profile" item folds into this button rather than being repeated a
 * second time as a settings row, avoiding the exact same action appearing
 * twice on one screen.
 *
 * [userName]/[userEmail] come straight from Authentication's existing
 * session (this sprint's scope: reuse, never duplicate, the signed-in
 * user's identity). The avatar has no photo URL anywhere in
 * [com.lifeos.app.features.auth.domain.model.AuthUser] yet, so this is
 * always [AppAvatar]'s initials/placeholder-icon fallback — literally the
 * "Avatar placeholder" this sprint's content list asks for.
 */
@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppAvatar(
            imageUrl = null,
            contentDescription = ProfileStrings.AVATAR_CONTENT_DESCRIPTION,
            size = LifeOSSize.avatarLarge,
            initials = userName.initialsOrNull(),
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        Text(text = userName, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        AppOutlinedButton(text = ProfileStrings.EDIT_PROFILE_ACTION, onClick = onEditProfileClick)
    }
}

/** "Bahar Toslak" -> "BT"; falls back to `null` (AppAvatar's own person-icon fallback) for a blank name. */
private fun String.initialsOrNull(): String? {
    val initials = trim().split(" ").filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull() }
    return initials.joinToString("").uppercase().ifBlank { null }
}

@Preview
@Composable
private fun ProfileHeaderPreview() {
    LifeOSTheme {
        ProfileHeader(
            userName = "Bahar Toslak",
            userEmail = "bahar.toslak@lifeos.app",
            onEditProfileClick = {},
        )
    }
}
