package com.lifeos.app.features.profile.presentation.changepassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.components.PasswordTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.profile.presentation.ProfileStrings
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/** Pushed from Profile's "Şifre Değiştir" row; [onPasswordChanged] closes it and lets Profile confirm. */
@Composable
fun ChangePasswordRoute(
    onNavigateBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    viewModel: ChangePasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CollectActions(viewModel.actions) { action ->
        when (action) {
            ChangePasswordAction.PasswordChanged -> onPasswordChanged()
        }
    }

    ChangePasswordScreen(uiState = uiState, onEvent = viewModel::onEvent, onNavigateBack = onNavigateBack)
}

@Composable
private fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    onEvent: (ChangePasswordEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = ProfileStrings.CHANGE_PASSWORD_TITLE,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = ProfileStrings.CHANGE_PASSWORD_BACK,
                onNavigationClick = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LifeOSSpacing.lg, vertical = LifeOSSpacing.md),
            verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg),
        ) {
            Text(
                text = ProfileStrings.CHANGE_PASSWORD_INFO,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PasswordTextField(
                value = uiState.currentPassword,
                onValueChange = { onEvent(ChangePasswordEvent.CurrentPasswordChanged(it)) },
                label = ProfileStrings.CURRENT_PASSWORD_LABEL,
                isError = uiState.currentPasswordError != null,
                supportingText = uiState.currentPasswordError,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            PasswordTextField(
                value = uiState.newPassword,
                onValueChange = { onEvent(ChangePasswordEvent.NewPasswordChanged(it)) },
                label = ProfileStrings.NEW_PASSWORD_LABEL,
                isError = uiState.newPasswordError != null,
                supportingText = uiState.newPasswordError,
            )
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(ChangePasswordEvent.ConfirmPasswordChanged(it)) },
                label = ProfileStrings.CONFIRM_NEW_PASSWORD_LABEL,
                isError = uiState.confirmPasswordError != null,
                supportingText = uiState.confirmPasswordError,
            )
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            AppPrimaryButton(
                text = if (uiState.isSaving) ProfileStrings.CHANGE_PASSWORD_IN_PROGRESS else ProfileStrings.CHANGE_PASSWORD_SUBMIT,
                onClick = { onEvent(ChangePasswordEvent.SubmitClicked) },
                enabled = !uiState.isSaving,
                loading = uiState.isSaving,
            )
        }
    }
}

@Preview
@Composable
private fun ChangePasswordScreenPreview() {
    LifeOSTheme {
        ChangePasswordScreen(
            uiState = ChangePasswordUiState(currentPasswordError = ProfileStrings.CURRENT_PASSWORD_WRONG),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
