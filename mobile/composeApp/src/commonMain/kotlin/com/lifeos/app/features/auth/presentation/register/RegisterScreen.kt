package com.lifeos.app.features.auth.presentation.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Checkbox
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
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppSnackbarHost
import com.lifeos.app.core.designsystem.components.AppTextField
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
fun RegisterRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectActions(viewModel.actions) { action ->
        when (action) {
            RegisterAction.NavigateToLogin -> onNavigateToLogin()
            RegisterAction.NavigateToHome -> onNavigateToHome()
            is RegisterAction.ShowMessage -> snackbarHostState.showSnackbar(action.message)
        }
    }

    RegisterScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun RegisterScreen(
    uiState: RegisterUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (RegisterEvent) -> Unit,
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
            AuthScreenTitle(title = AuthStrings.REGISTER_TITLE, subtitle = AuthStrings.REGISTER_SUBTITLE)
            Spacer(modifier = Modifier.height(LifeOSSpacing.xl))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                AppTextField(
                    value = uiState.fullName,
                    onValueChange = { onEvent(RegisterEvent.FullNameChanged(it)) },
                    label = AuthStrings.REGISTER_FULL_NAME_LABEL,
                    placeholder = AuthStrings.REGISTER_FULL_NAME_PLACEHOLDER,
                    leadingIcon = Icons.Filled.Badge,
                    isError = uiState.fullNameError != null,
                    supportingText = uiState.fullNameError,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                AuthEmailField(
                    value = uiState.email,
                    onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
                    error = uiState.emailError,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                PasswordTextField(
                    value = uiState.password,
                    onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
                    label = AuthStrings.LOGIN_PASSWORD_LABEL,
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                PasswordTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                    label = AuthStrings.REGISTER_CONFIRM_PASSWORD_LABEL,
                    isError = uiState.confirmPasswordError != null,
                    supportingText = uiState.confirmPasswordError,
                )
                Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = uiState.isTermsAccepted,
                        onCheckedChange = { onEvent(RegisterEvent.TermsAcceptedChanged(it)) },
                    )
                    Text(
                        text = AuthStrings.REGISTER_TERMS_TEXT,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            onEvent(RegisterEvent.TermsAcceptedChanged(!uiState.isTermsAccepted))
                        },
                    )
                }
                if (uiState.termsError != null) {
                    Text(
                        text = uiState.termsError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
                AppPrimaryButton(
                    text = AuthStrings.REGISTER_SUBMIT,
                    onClick = { onEvent(RegisterEvent.RegisterClicked) },
                    enabled = uiState.isSubmitEnabled,
                    loading = uiState.isLoading,
                )
            }

            Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
            AuthFooterPrompt(
                promptText = AuthStrings.REGISTER_HAVE_ACCOUNT,
                actionText = AuthStrings.REGISTER_LOGIN,
                onActionClick = { onEvent(RegisterEvent.LoginClicked) },
            )
        }
    }
}

@Preview
@Composable
private fun RegisterScreenPreview() {
    LifeOSTheme {
        RegisterScreen(
            uiState = RegisterUiState(fullName = "Ayşe Yılmaz", email = "ayse@ornek.com"),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun RegisterScreenLoadingPreview() {
    LifeOSTheme {
        RegisterScreen(
            uiState = RegisterUiState(
                fullName = "Ayşe Yılmaz",
                email = "ayse@ornek.com",
                isTermsAccepted = true,
                isLoading = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun RegisterScreenValidationErrorPreview() {
    LifeOSTheme {
        RegisterScreen(
            uiState = RegisterUiState(
                fullNameError = "Ad soyad en az 2 karakter olmalı.",
                emailError = "Geçerli bir e-posta adresi girin.",
                passwordError = "Şifre en az 8 karakter olmalı.",
                confirmPasswordError = "Şifreler eşleşmiyor.",
                termsError = AuthStrings.TERMS_NOT_ACCEPTED_ERROR,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
