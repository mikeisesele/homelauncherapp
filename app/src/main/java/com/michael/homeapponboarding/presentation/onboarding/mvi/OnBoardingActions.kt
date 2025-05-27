package com.michael.homeapponboarding.presentation.onboarding.mvi

/**
 * MVI Actions for Onboarding
 * Represents all possible user actions and system events
 */
sealed class OnboardingAction {

    // User Actions
    data object ContinueClicked : OnboardingAction()
    data object RetryHomeSelection : OnboardingAction()

    // System Events
    data object Initialize : OnboardingAction()
    data object ActivityResumed : OnboardingAction()
    data object NewIntentReceived : OnboardingAction()
    data class HomeSelectionError(val message: String) : OnboardingAction()

    // State Updates
    data class DefaultHomeStatusChanged(val isDefault: Boolean) : OnboardingAction()
    data class OnboardingCompletionChanged(val isCompleted: Boolean) : OnboardingAction()

    // Internal Actions (used by reducer)
    internal data object MoveToNextStep : OnboardingAction()
    internal data object FinishOnboarding : OnboardingAction()
    internal data object FinishActivity : OnboardingAction()
    internal data class UpdateStep(val step: OnboardingStep) : OnboardingAction()
}