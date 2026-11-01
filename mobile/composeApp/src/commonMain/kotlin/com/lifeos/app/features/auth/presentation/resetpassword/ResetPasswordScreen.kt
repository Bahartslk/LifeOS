package com.lifeos.app.features.auth.presentation.resetpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.components.PasswordTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.auth.presentation.AuthStrings
import com.lifeos.app.features.auth.presentation.common.AuthFormMessage
import com.lifeos.app.features.auth.presentation.common.AuthScreenTitle
import com.lifeos.app.features.auth.presentation.common.AuthWordmark
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Opened from Forgot Password with the address the code was sent to. */
@Composable
fun ResetPasswordRoute(
    email: String,
    onPasswordReset: () -> Unit,
    viewModel: ResetPasswordViewModel = koinViewModel(parameters = { parametersOf(email) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    CollectActions(viewModel.actions) { action ->
        when (action) {
            ResetPasswordAction.NavigateToLogin -> onPasswordReset()
        }
    }

    ResetPasswordScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun ResetPasswordScreen(
    uiState: ResetPasswordUiState,
    onEvent: (ResetPasswordEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(LifeOSSpacing.lg),
    ) {
        AuthWordmark()
        Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
        AuthScreenTitle(
            title = AuthStrings.RESET_PASSWORD_TITLE,
            subtitle = AuthStrings.resetPasswordSubtitle(uiState.email),
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.xl))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppTextField(
                value = uiState.code,
                onValueChange = { onEvent(ResetPasswordEvent.CodeChanged(it)) },
                label = AuthStrings.RESET_PASSWORD_CODE_LABEL,
                placeholder = AuthStrings.RESET_PASSWORD_CODE_PLACEHOLDER,
                leadingIcon = Icons.Filled.Pin,
                isError = uiState.codeError != null,
                supportingText = uiState.codeError,
                keyboardType = KeyboardType.NumberPassword,
            )
            Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
            PasswordTextField(
                value = uiState.newPassword,
                onValueChange = { onEvent(ResetPasswordEvent.NewPasswordChanged(it)) },
                label = AuthStrings.RESET_PASSWORD_NEW_PASSWORD_LABEL,
                isError = uiState.newPasswordError != null,
                supportingText = uiState.newPasswordError,
            )
            Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(ResetPasswordEvent.ConfirmPasswordChanged(it)) },
                label = AuthStrings.RESET_PASSWORD_CONFIRM_LABEL,
                isError = uiState.confirmPasswordError != null,
                supportingText = uiState.confirmPasswordError,
            )
            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(LifeOSSpacing.md))
                AuthFormMessage(text = message, isError = true)
            }
            uiState.infoMessage?.let { message ->
                Spacer(modifier = Modifier.height(LifeOSSpacing.md))
                AuthFormMessage(text = message, isError = false)
            }
            Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
            AppPrimaryButton(
                text = AuthStrings.RESET_PASSWORD_SUBMIT,
                onClick = { onEvent(ResetPasswordEvent.SubmitClicked) },
                enabled = !uiState.isSubmitting,
                loading = uiState.isSubmitting,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppOutlinedButton(
            text = AuthStrings.RESET_PASSWORD_RESEND,
            onClick = { onEvent(ResetPasswordEvent.ResendClicked) },
            enabled = !uiState.isResending && !uiState.isSubmitting,
        )
    }
}

@Preview
@Composable
private fun ResetPasswordScreenPreview() {
    LifeOSTheme {
        ResetPasswordScreen(
            uiState = ResetPasswordUiState(
                email = "bahar@example.com",
                codeError = AuthStrings.RESET_PASSWORD_CODE_WRONG,
            ),
            onEvent = {},
        )
    }
}
