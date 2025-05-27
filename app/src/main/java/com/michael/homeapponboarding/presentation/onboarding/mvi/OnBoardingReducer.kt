package com.michael.homeapponboarding.presentation.onboarding.mvi

import android.util.Log
import com.michael.homeapponboarding.AppLogger

/**
 * MVI Reducer for Onboarding
 * Pure function that takes current state and action, returns new state
 * Handles all state transitions according to exercise requirements
 */
object OnboardingReducer {

    private const val TAG = "OnboardingReducer"

    /**
     * Main reducer function - pure state transition logic
     */
    fun reduce(currentState: OnboardingState, action: OnboardingAction): OnboardingState {
        Log.d(TAG, "reduce: Current state: $currentState")
        Log.d(TAG, "reduce: Action: $action")

        val newState = when (action) {
            is OnboardingAction.Initialize -> handleInitialize(currentState)
            is OnboardingAction.ActivityResumed -> handleActivityResumed(currentState)
            is OnboardingAction.ContinueClicked -> handleContinueClicked(currentState)
            is OnboardingAction.RetryHomeSelection -> handleRetryHomeSelection(currentState)
            is OnboardingAction.HomeSelectionError -> handleHomeSelectionError(currentState)
            is OnboardingAction.NewIntentReceived -> handleNewIntentReceived(currentState)
            is OnboardingAction.DefaultHomeStatusChanged -> handleDefaultHomeStatusChanged(currentState, action.isDefault)
            is OnboardingAction.OnboardingCompletionChanged -> handleOnboardingCompletionChanged(currentState, action.isCompleted)

            // Internal actions
            is OnboardingAction.MoveToNextStep -> handleMoveToNextStep(currentState)
            is OnboardingAction.FinishOnboarding -> handleFinishOnboarding(currentState)
            is OnboardingAction.FinishActivity -> handleFinishActivity(currentState)
            is OnboardingAction.UpdateStep -> handleUpdateStep(currentState, action.step)
        }

        Log.d(TAG, "reduce: New state: $newState")
        return newState
    }

    private fun handleInitialize(state: OnboardingState): OnboardingState {
        AppLogger.state(TAG, "Initialize", mapOf(
            "step" to state.currentStep,
            "isDefault" to state.isSetAsDefaultHome,
            "completed" to state.isOnboardingCompleted
        ))

        return when {
            // Case 1: Completed and default - should finish
            state.isSetAsDefaultHome && state.isOnboardingCompleted -> {
                AppLogger.d(TAG, "handleInitialize: Completed and default - finishing")
                state.copy(shouldFinishActivity = true)
            }

            // Case 2: Default but not completed - go to ThankYou
            state.isSetAsDefaultHome && !state.isOnboardingCompleted -> {
                AppLogger.d(TAG, "handleInitialize: Default but not completed - ThankYou")
                state.copy(
                    currentStep = OnboardingStep.ThankYou,
                    isWaitingForHomeSelection = false
                )
            }

            // Case 3: Not default and not completed
            !state.isSetAsDefaultHome && !state.isOnboardingCompleted -> {
                AppLogger.d(TAG, "handleInitialize: Not default, not completed - keeping ViewModel step: ${state.currentStep}")
                state.copy(isWaitingForHomeSelection = false)
            }

            else -> {
                AppLogger.d(TAG, "handleInitialize: Default case - Welcome")
                state.copy(
                    currentStep = OnboardingStep.Welcome,
                    isWaitingForHomeSelection = false
                )
            }
        }
    }

    private fun handleActivityResumed(state: OnboardingState): OnboardingState {
        Log.d(TAG, "handleActivityResumed: Current state - step: ${state.currentStep}, waiting: ${state.isWaitingForHomeSelection}, isDefault: ${state.isSetAsDefaultHome}, completed: ${state.isOnboardingCompleted}")

        // If onboarding completed externally, finish activity
        if (state.isOnboardingCompleted && state.isSetAsDefaultHome) {
            Log.d(TAG, "handleActivityResumed: Onboarding completed externally, should finish")
            return state.copy(shouldFinishActivity = true)
        }

        // CASE 1: We were waiting for home selection and now we're set as default
        if (state.isWaitingForHomeSelection && state.isSetAsDefaultHome) {
            Log.d(TAG, "handleActivityResumed: User set app as default through guided flow, proceeding to step 3")
            return state.copy(
                isWaitingForHomeSelection = false,
                currentStep = OnboardingStep.ThankYou
            )
        }

        // CASE 2: We were waiting but user returned without setting as default
        if (state.isWaitingForHomeSelection && !state.isSetAsDefaultHome) {
            Log.d(TAG, "handleActivityResumed: User returned without setting as default, allowing retry")
            return state.copy(isWaitingForHomeSelection = false)
        }

        // CASE 3: NEW - App became default but we weren't waiting (e.g., through system dialog)
        // This happens when user is at Welcome/SetDefault and app becomes default
        if (!state.isWaitingForHomeSelection &&
            state.isSetAsDefaultHome &&
            !state.isOnboardingCompleted &&
            state.currentStep != OnboardingStep.ThankYou) {

            Log.d(TAG, "handleActivityResumed: App became default (not through waiting), proceeding to step 3")
            return state.copy(currentStep = OnboardingStep.ThankYou)
        }

        // CASE 4: Keep current state
        Log.d(TAG, "handleActivityResumed: No state changes needed")
        return state
    }
    private fun handleContinueClicked(state: OnboardingState): OnboardingState {
        return when (state.currentStep) {
            OnboardingStep.Welcome -> {
                // EXERCISE REQUIREMENT: Go to step 2
                Log.d(TAG, "handleContinueClicked: Moving from Welcome to SetDefault")
                state.copy(currentStep = OnboardingStep.SetDefault)
            }
            OnboardingStep.SetDefault -> {
                // EXERCISE REQUIREMENT: Open system home app selection
                Log.d(TAG, "handleContinueClicked: Starting home selection process")
                state.copy(
                    isWaitingForHomeSelection = true,
                )
            }
            OnboardingStep.ThankYou -> {
                // EXERCISE REQUIREMENT: Finish onboarding
                Log.d(TAG, "handleContinueClicked: Finishing onboarding")
                state.copy(
                    isOnboardingCompleted = true,
                    shouldFinishActivity = true
                )
            }
        }
    }

    private fun handleRetryHomeSelection(state: OnboardingState): OnboardingState {
        return state.copy(
            isWaitingForHomeSelection = false,
        )
    }


    private fun handleHomeSelectionError(state: OnboardingState): OnboardingState {
        return state.copy(
            isWaitingForHomeSelection = false,
        )
    }


    private fun handleNewIntentReceived(state: OnboardingState): OnboardingState {
        if (state.isSetAsDefaultHome) {
            return if (!state.isOnboardingCompleted) {
                Log.d(TAG, "handleNewIntentReceived: Set as default, going to step 3")
                state.copy(
                    currentStep = OnboardingStep.ThankYou,
                    isWaitingForHomeSelection = false
                )
            } else {
                Log.d(TAG, "handleNewIntentReceived: Onboarding already completed, should finish")
                state.copy(shouldFinishActivity = true)
            }
        }
        return state
    }

    private fun handleDefaultHomeStatusChanged(state: OnboardingState, isDefault: Boolean): OnboardingState {
        return state.copy(isSetAsDefaultHome = isDefault)
    }

    private fun handleOnboardingCompletionChanged(state: OnboardingState, isCompleted: Boolean): OnboardingState {
        return state.copy(isOnboardingCompleted = isCompleted)
    }

    // Internal action handlers
    private fun handleMoveToNextStep(state: OnboardingState): OnboardingState {
        val nextStep = when (state.currentStep) {
            OnboardingStep.Welcome -> OnboardingStep.SetDefault
            OnboardingStep.SetDefault -> OnboardingStep.ThankYou
            OnboardingStep.ThankYou -> OnboardingStep.ThankYou // Stay on thank you
        }
        return state.copy(currentStep = nextStep)
    }

    private fun handleFinishOnboarding(state: OnboardingState): OnboardingState {
        return state.copy(
            isOnboardingCompleted = true,
            shouldFinishActivity = true
        )
    }

    private fun handleFinishActivity(state: OnboardingState): OnboardingState {
        return state.copy(shouldFinishActivity = true)
    }

    private fun handleUpdateStep(state: OnboardingState, step: OnboardingStep): OnboardingState {
        return state.copy(currentStep = step)
    }
}