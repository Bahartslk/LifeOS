package com.lifeos.app.features.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AppIcon
import com.lifeos.app.core.designsystem.theme.LifeOSGradients
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTextStyles
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.auth.presentation.AuthStrings
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful entry point wired into the nav graph — collects [SplashViewModel]
 * and forwards its one-shot [SplashAction]s to the navigation callbacks.
 */
@Composable
fun SplashRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    CollectActions(viewModel.actions) { action ->
        when (action) {
            SplashAction.NavigateToOnboarding -> onNavigateToOnboarding()
            SplashAction.NavigateToLogin -> onNavigateToLogin()
            SplashAction.NavigateToHome -> onNavigateToHome()
        }
    }
    SplashScreen()
}

/**
 * The stateless, previewable UI — matches splash.png: a full-bleed violet
 * gradient, the brand mark, and a syncing indicator near the bottom. Takes
 * no state at all: it's on display only for the brief moment the session
 * check takes, and always renders the same regardless of how far that
 * check has progressed.
 */
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LifeOSGradients.hero),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LifeOSSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
                Text(
                    text = AuthStrings.APP_NAME,
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    modifier = Modifier.padding(top = LifeOSSpacing.lg),
                )
                Text(
                    text = AuthStrings.SPLASH_TAGLINE,
                    style = LifeOSTextStyles.overline,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = LifeOSSpacing.xs),
                )
            }

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LifeOSSpacing.lg),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f),
            )
            Text(
                text = AuthStrings.SPLASH_SYNCING,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = AuthStrings.SPLASH_SUBTITLE,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    LifeOSTheme {
        SplashScreen()
    }
}
