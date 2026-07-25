package com.lifeos.app.features.auth.presentation.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.EyebrowChip
import com.lifeos.app.core.designsystem.components.GradientCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.presentation.CollectActions
import com.lifeos.app.features.auth.presentation.AuthStrings
import com.lifeos.app.features.auth.presentation.common.AuthWordmark
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingRoute(
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { uiState.pages.size })

    CollectActions(viewModel.actions) { action ->
        when (action) {
            OnboardingAction.NavigateToLogin -> onNavigateToLogin()
            is OnboardingAction.ScrollToPage -> pagerState.animateScrollToPage(action.page)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(OnboardingEvent.PageChanged(pagerState.currentPage))
    }

    OnboardingScreen(
        uiState = uiState,
        pagerState = pagerState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun OnboardingScreen(
    uiState: OnboardingUiState,
    pagerState: PagerState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(LifeOSSpacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthWordmark()
            if (!uiState.isLastPage) {
                Text(
                    text = AuthStrings.ONBOARDING_SKIP,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { onEvent(OnboardingEvent.SkipClicked) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { pageIndex ->
            val page = uiState.pages[pageIndex]
            OnboardingPageContentView(page)
        }

        OnboardingPagerIndicator(
            pageCount = uiState.pages.size,
            currentPage = uiState.currentPage,
            modifier = Modifier.padding(vertical = LifeOSSpacing.lg),
        )

        AppPrimaryButton(
            text = if (uiState.isLastPage) AuthStrings.ONBOARDING_GET_STARTED else nextLabel(uiState),
            onClick = {
                onEvent(if (uiState.isLastPage) OnboardingEvent.GetStartedClicked else OnboardingEvent.NextClicked)
            },
            trailingIcon = Icons.Filled.ArrowForward,
        )

        if (uiState.isLastPage) {
            Text(
                text = AuthStrings.ONBOARDING_LEGAL_FOOTER,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = LifeOSSpacing.md),
            )
        }
    }
}

private fun nextLabel(uiState: OnboardingUiState): String =
    if (uiState.currentPage == 0) AuthStrings.ONBOARDING_NEXT_STEP else AuthStrings.ONBOARDING_NEXT

@Composable
private fun OnboardingPageContentView(page: OnboardingPageContent) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = LifeOSSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GradientCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = AuthStrings.APP_NAME,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(LifeOSSpacing.xl))

        if (page.eyebrow != null) {
            EyebrowChip(label = page.eyebrow)
            Spacer(modifier = Modifier.height(LifeOSSpacing.md))
        }
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    LifeOSTheme {
        val pages = listOf(
            OnboardingPageContent(
                eyebrow = AuthStrings.ONBOARDING_PAGE1_EYEBROW,
                title = AuthStrings.ONBOARDING_PAGE1_TITLE,
                description = AuthStrings.ONBOARDING_PAGE1_DESCRIPTION,
            ),
        )
        OnboardingScreen(
            uiState = OnboardingUiState(currentPage = 0, pages = pages),
            pagerState = rememberPagerState(pageCount = { pages.size }),
            onEvent = {},
        )
    }
}
