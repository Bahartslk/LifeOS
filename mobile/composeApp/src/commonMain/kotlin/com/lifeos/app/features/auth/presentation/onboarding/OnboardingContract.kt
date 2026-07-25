package com.lifeos.app.features.auth.presentation.onboarding

/** Static per-page copy — presentation-only content, not a domain model. */
data class OnboardingPageContent(
    val eyebrow: String?,
    val title: String,
    val description: String,
)

data class OnboardingUiState(
    val currentPage: Int = 0,
    val pages: List<OnboardingPageContent> = emptyList(),
) {
    val isLastPage: Boolean get() = currentPage == pages.lastIndex
}

sealed interface OnboardingEvent {
    data class PageChanged(val page: Int) : OnboardingEvent
    data object SkipClicked : OnboardingEvent
    data object NextClicked : OnboardingEvent
    data object GetStartedClicked : OnboardingEvent
}

sealed interface OnboardingAction {
    data object NavigateToLogin : OnboardingAction
    data class ScrollToPage(val page: Int) : OnboardingAction
}
