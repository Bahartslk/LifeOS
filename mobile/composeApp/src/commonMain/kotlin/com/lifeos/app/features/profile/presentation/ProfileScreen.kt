package com.lifeos.app.features.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.ConfirmationDialog
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.SkeletonListContent
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.profile.presentation.sections.AboutSection
import com.lifeos.app.features.profile.presentation.sections.AccountSection
import com.lifeos.app.features.profile.presentation.sections.NotificationsSection
import com.lifeos.app.features.profile.presentation.sections.PreferencesSection
import com.lifeos.app.features.profile.presentation.sections.ProfileHeader
import com.lifeos.app.features.profile.presentation.sections.SessionSection
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into `core/navigation/MainNavGraph.kt` —
 * collects [ProfileViewModel] and forwards its one-shot [ProfileAction]s to
 * a snackbar, per the same Route/Screen split every other feature follows.
 * No navigation callbacks: every action on this screen is either a
 * "coming soon" message or the real, self-contained theme selector — none
 * of them navigate anywhere else, per this sprint's scope.
 */
@Composable
fun ProfileRoute(
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            is ProfileAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
            ProfileAction.NavigateToLogin -> onLoggedOut()
        }
    }

    ProfileScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

/** The stateless, previewable screen. Composed entirely from section composables (per this project's "no single huge screen" rule). */
@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ProfileEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.errorMessage != null -> ErrorView(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                description = uiState.errorMessage,
                onRetry = { onEvent(ProfileEvent.RetryClicked) },
            )
            uiState.isLoading -> SkeletonListContent(
                count = LOADING_SKELETON_COUNT,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            else -> ProfileContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }

    if (uiState.isLogoutConfirmationVisible) {
        ConfirmationDialog(
            title = ProfileStrings.LOGOUT_CONFIRMATION_TITLE,
            message = ProfileStrings.LOGOUT_CONFIRMATION_MESSAGE,
            confirmLabel = ProfileStrings.LOGOUT_CONFIRMATION_CONFIRM,
            dismissLabel = ProfileStrings.LOGOUT_CONFIRMATION_DISMISS,
            onConfirm = { onEvent(ProfileEvent.LogoutConfirmed) },
            onDismiss = { onEvent(ProfileEvent.LogoutDismissed) },
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = LifeOSSpacing.lg, vertical = LifeOSSpacing.md),
        verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.xl),
    ) {
        item {
            ProfileHeader(
                userName = uiState.userName,
                userEmail = uiState.userEmail,
                onEditProfileClick = { onEvent(ProfileEvent.EditProfileClicked) },
            )
        }
        item {
            AccountSection(
                onSecurityClick = { onEvent(ProfileEvent.SecurityClicked) },
                onChangePasswordClick = { onEvent(ProfileEvent.ChangePasswordClicked) },
            )
        }
        item {
            PreferencesSection(
                themeMode = uiState.themeMode,
                onThemeModeSelected = { mode -> onEvent(ProfileEvent.AppearanceOptionSelected(mode)) },
                onLanguageClick = { onEvent(ProfileEvent.LanguageClicked) },
                onAiPreferencesClick = { onEvent(ProfileEvent.AiPreferencesClicked) },
                onTravelPreferencesClick = { onEvent(ProfileEvent.TravelPreferencesClicked) },
            )
        }
        item {
            NotificationsSection(
                onNotificationSettingsClick = { onEvent(ProfileEvent.NotificationSettingsClicked) },
                onReminderSettingsClick = { onEvent(ProfileEvent.ReminderSettingsClicked) },
            )
        }
        item {
            AboutSection(
                onPrivacyPolicyClick = { onEvent(ProfileEvent.PrivacyPolicyClicked) },
                onTermsOfServiceClick = { onEvent(ProfileEvent.TermsOfServiceClicked) },
            )
        }
        item {
            SessionSection(
                isLoggingOut = uiState.isLoggingOut,
                onLogoutClick = { onEvent(ProfileEvent.LogoutClicked) },
            )
        }
    }
}

private const val LOADING_SKELETON_COUNT = 4

@Preview
@Composable
private fun ProfileScreenLoadingPreview() {
    LifeOSTheme {
        ProfileScreen(
            uiState = ProfileUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun ProfileScreenErrorPreview() {
    LifeOSTheme {
        ProfileScreen(
            uiState = ProfileUiState(isLoading = false, errorMessage = ProfileStrings.LOAD_ERROR_MESSAGE),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    LifeOSTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                isLoading = false,
                userName = "Bahar Toslak",
                userEmail = "bahar.toslak@lifeos.app",
                themeMode = ThemeMode.SYSTEM,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

/** The Logout confirmation overlay, shown mid-screen. */
@Preview
@Composable
private fun ProfileScreenLogoutConfirmationPreview() {
    LifeOSTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                isLoading = false,
                userName = "Bahar Toslak",
                userEmail = "bahar.toslak@lifeos.app",
                themeMode = ThemeMode.DARK,
                isLogoutConfirmationVisible = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
