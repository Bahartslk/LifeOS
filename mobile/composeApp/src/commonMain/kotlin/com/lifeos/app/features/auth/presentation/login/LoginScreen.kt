package com.lifeos.app.features.auth.presentation.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.PasswordTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.auth.presentation.AuthStrings
import com.lifeos.app.features.auth.presentation.common.AuthEmailField
import com.lifeos.app.features.auth.presentation.common.AuthFooterPrompt
import com.lifeos.app.features.auth.presentation.common.AuthScreenTitle
import com.lifeos.app.features.auth.presentation.common.AuthWordmark
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoute(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            LoginAction.NavigateToRegister -> onNavigateToRegister()
            LoginAction.NavigateToForgotPassword -> onNavigateToForgotPassword()
            LoginAction.NavigateToHome -> onNavigateToHome()
            is LoginAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    LoginScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun LoginScreen(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (LoginEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(LifeOSSpacing.lg),
        ) {
            AuthWordmark()
            Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
            AuthScreenTitle(title = AuthStrings.LOGIN_TITLE, subtitle = AuthStrings.LOGIN_SUBTITLE)
            Spacer(modifier = Modifier.height(LifeOSSpacing.xl))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                AuthEmailField(
                    value = uiState.email,
                    onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                    error = uiState.emailError,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                PasswordTextField(
                    value = uiState.password,
                    onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                    label = AuthStrings.LOGIN_PASSWORD_LABEL,
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError,
                    forgotPasswordLabel = AuthStrings.LOGIN_FORGOT_PASSWORD,
                    onForgotPasswordClick = { onEvent(LoginEvent.ForgotPasswordClicked) },
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
                AppPrimaryButton(
                    text = AuthStrings.LOGIN_SUBMIT,
                    onClick = { onEvent(LoginEvent.LoginClicked) },
                    enabled = uiState.isSubmitEnabled,
                    loading = uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                OrDivider()
                Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                AppOutlinedButton(
                    text = AuthStrings.LOGIN_CONTINUE_WITH_GOOGLE,
                    onClick = { onEvent(LoginEvent.ContinueWithGoogleClicked) },
                )
            }

            Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
            AuthFooterPrompt(
                promptText = AuthStrings.LOGIN_NO_ACCOUNT,
                actionText = AuthStrings.LOGIN_CREATE_ACCOUNT,
                onActionClick = { onEvent(LoginEvent.RegisterClicked) },
            )
        }
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = AuthStrings.LOGIN_OR_DIVIDER,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = LifeOSSpacing.sm),
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LifeOSTheme {
        LoginScreen(
            uiState = LoginUiState(email = "demo@lifeos.app"),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    LifeOSTheme {
        LoginScreen(
            uiState = LoginUiState(email = "demo@lifeos.app", isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenValidationErrorPreview() {
    LifeOSTheme {
        LoginScreen(
            uiState = LoginUiState(
                email = "invalid-email",
                emailError = "Geçerli bir e-posta adresi girin.",
                passwordError = "Şifre en az 8 karakter olmalı.",
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
