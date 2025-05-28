package com.michael.homeapponboarding.presentation.onboarding.mvi

/**
 * MVI State for Onboarding
 * Represents the complete state of the onboarding flow
 */
data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val isWaitingForHomeSelection: Boolean = false,
    val shouldFinishActivity: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val isSetAsDefaultHome: Boolean = false
)

/**
 * Represents the onboarding steps as per exercise requirements
 */
sealed class OnboardingStep(val stepNumber: Int) {
    data object Welcome : OnboardingStep(1)
    data object SetDefault : OnboardingStep(2)
    data object ThankYou : OnboardingStep(3)

    companion object {
        fun fromInt(step: Int): OnboardingStep {
            return when (step) {
                1 -> Welcome
                2 -> SetDefault
                3 -> ThankYou
                else -> Welcome
            }
        }
    }
}