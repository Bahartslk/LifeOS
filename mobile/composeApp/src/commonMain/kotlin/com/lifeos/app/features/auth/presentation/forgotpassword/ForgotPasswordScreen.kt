package com.lifeos.app.features.auth.presentation.forgotpassword

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.auth.presentation.AuthStrings
import com.lifeos.app.features.auth.presentation.common.AuthFormMessage
import com.lifeos.app.features.auth.presentation.common.AuthScreenTitle
import com.lifeos.app.features.auth.presentation.common.AuthWordmark
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToResetPassword: (email: String) -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CollectActions(viewModel.actions) { action ->
        when (action) {
            ForgotPasswordAction.NavigateToLogin -> onNavigateToLogin()
            is ForgotPasswordAction.NavigateToResetPassword -> onNavigateToResetPassword(action.email)
        }
    }

    ForgotPasswordScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
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
            title = AuthStrings.FORGOT_PASSWORD_TITLE,
            subtitle = AuthStrings.FORGOT_PASSWORD_SUBTITLE,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.xl))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppTextField(
                value = uiState.email,
                onValueChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                label = AuthStrings.LOGIN_EMAIL_LABEL,
                placeholder = AuthStrings.LOGIN_EMAIL_PLACEHOLDER,
                leadingIcon = Icons.Filled.Email,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError,
                keyboardType = KeyboardType.Email,
            )
            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(LifeOSSpacing.md))
                AuthFormMessage(text = message, isError = true)
            }
            Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
            AppPrimaryButton(
                text = AuthStrings.FORGOT_PASSWORD_SUBMIT,
                onClick = { onEvent(ForgotPasswordEvent.SubmitClicked) },
                enabled = uiState.isSubmitEnabled,
                loading = uiState.isLoading,
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        AppOutlinedButton(
            text = AuthStrings.FORGOT_PASSWORD_BACK_TO_LOGIN,
            onClick = { onEvent(ForgotPasswordEvent.BackToLoginClicked) },
        )
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenPreview() {
    LifeOSTheme {
        ForgotPasswordScreen(uiState = ForgotPasswordUiState(), onEvent = {})
    }
}

@Preview
@Composable
private fun ForgotPasswordRateLimitedPreview() {
    LifeOSTheme {
        ForgotPasswordScreen(
            uiState = ForgotPasswordUiState(errorMessage = AuthStrings.PASSWORD_RESET_TOO_MANY_ATTEMPTS),
            onEvent = {},
        )
    }
}
