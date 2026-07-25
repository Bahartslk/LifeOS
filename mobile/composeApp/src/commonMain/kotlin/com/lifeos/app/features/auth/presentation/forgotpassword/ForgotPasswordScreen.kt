package com.lifeos.app.features.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTealBase
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.auth.presentation.AuthStrings
import com.lifeos.app.features.auth.presentation.common.AuthScreenTitle
import com.lifeos.app.features.auth.presentation.common.AuthWordmark
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordRoute(
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CollectActions(viewModel.actions) { action ->
        when (action) {
            ForgotPasswordAction.NavigateToLogin -> onNavigateToLogin()
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
            .padding(LifeOSSpacing.lg),
    ) {
        AuthWordmark()
        Spacer(modifier = Modifier.height(LifeOSSpacing.xl))

        if (uiState.isSuccess) {
            ForgotPasswordSuccessContent(onEvent = onEvent)
        } else {
            ForgotPasswordFormContent(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun ForgotPasswordFormContent(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
) {
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
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
        AppPrimaryButton(
            text = AuthStrings.FORGOT_PASSWORD_SUBMIT,
            onClick = { onEvent(ForgotPasswordEvent.SubmitClicked) },
            enabled = uiState.isSubmitEnabled,
            loading = uiState.isLoading,
        )
    }
}

@Composable
private fun ForgotPasswordSuccessContent(onEvent: (ForgotPasswordEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = LifeOSTealBase,
            size = LifeOSSize.iconLarge,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Text(
            text = AuthStrings.FORGOT_PASSWORD_SUCCESS_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(
            text = AuthStrings.FORGOT_PASSWORD_SUCCESS_DESCRIPTION,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.xl))
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
private fun ForgotPasswordSuccessPreview() {
    LifeOSTheme {
        ForgotPasswordScreen(uiState = ForgotPasswordUiState(isSuccess = true), onEvent = {})
    }
}
