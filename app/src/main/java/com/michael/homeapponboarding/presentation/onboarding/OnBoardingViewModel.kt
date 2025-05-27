package com.michael.homeapponboarding.presentation.onboarding

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michael.homeapponboarding.AppLogger
import com.michael.homeapponboarding.data.HomeAppRepository
import com.michael.homeapponboarding.presentation.onboarding.mvi.OnboardingAction
import com.michael.homeapponboarding.presentation.onboarding.mvi.OnboardingReducer
import com.michael.homeapponboarding.presentation.onboarding.mvi.OnboardingState
import com.michael.homeapponboarding.presentation.onboarding.mvi.OnboardingStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: HomeAppRepository
) : ViewModel() {

    companion object {
        private const val TAG = "OnboardingViewModel"
    }

    // MVI State Flow
    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    val currentState: OnboardingState get() = _state.value

    // LiveData for one-time events
    private val _viewEvents = MutableLiveData<ViewEvent>()
    val viewEvents: LiveData<ViewEvent> = _viewEvents

    // Package name storage
    private var packageName: String = ""

    fun updateContextWithPackageName(packageName: String) {
        val isCurrentlyDefaultHome = repository.isSetAsDefaultHome(packageName)
        val onboardingCompleted = repository.isOnboardingCompleted()
        val isTrulyFresh = repository.isTrulyFreshInstall()

        val correctStep = when {
            // Check fresh install FIRST, before default home status
            isTrulyFresh -> {
                Log.d(TAG, "CASE 1: Truly fresh install - FORCING Welcome (ignoring default status)")
                OnboardingStep.Welcome
            }
            onboardingCompleted && isCurrentlyDefaultHome -> {
                Log.d(TAG, "CASE 2: Completed and default - should finish")
                // This should trigger finish activity
                currentState.currentStep
            }
            !onboardingCompleted && isCurrentlyDefaultHome -> {
                Log.d(TAG, "CASE 3: Default but not completed (external set) - ThankYou")
                OnboardingStep.ThankYou
            }
            !onboardingCompleted && !isCurrentlyDefaultHome -> {
                if (currentState.currentStep == OnboardingStep.ThankYou) {
                    Log.d(TAG, "CASE 4: On ThankYou but not default - correcting to SetDefault")
                    OnboardingStep.SetDefault
                } else {
                    Log.d(TAG, "CASE 4: In-progress - keeping step ${currentState.currentStep}")
                    currentState.currentStep
                }
            }
            else -> {
                Log.d(TAG, "CASE 5: Other - keeping step ${currentState.currentStep}")
                currentState.currentStep
            }
        }

        // set shouldFinishActivity if completed and default
        val shouldFinish = onboardingCompleted && isCurrentlyDefaultHome

        val updatedState = currentState.copy(
            isSetAsDefaultHome = isCurrentlyDefaultHome,
            isOnboardingCompleted = onboardingCompleted,
            currentStep = correctStep,
            shouldFinishActivity = shouldFinish
        )

        updateState(updatedState)
    }

    /**
     * Mark onboarding as started (prevents fresh install resets)
     */
    fun markOnboardingStarted() {
        repository.markOnboardingStarted()
    }

    /**
     * Process MVI actions
     */
    private fun processAction(action: OnboardingAction) {
        AppLogger.d(TAG, "Processing action: $action")

        val newState = OnboardingReducer.reduce(currentState, action)
        updateState(newState)
        handleSideEffects(action, newState)
        persistState(newState)
    }

    // Action shortcuts
    fun initialize() = processAction(OnboardingAction.Initialize)
    fun onActivityResumed() = processAction(OnboardingAction.ActivityResumed)
    fun onContinueClicked() = processAction(OnboardingAction.ContinueClicked)
    fun onNewIntent() = processAction(OnboardingAction.NewIntentReceived)

    fun verifyHomeAppStatusDelayed() {
        if (packageName.isNotEmpty()) {
            updateContextWithPackageName(packageName)
        }
    }

    private fun createInitialState(): OnboardingState {
        val completed = repository.isOnboardingCompleted()
        val step = repository.getCurrentStep()
        val waiting = repository.isWaitingForHomeSelection()

        AppLogger.state(TAG, "InitialState", mapOf(
            "completed" to completed,
            "step" to step,
            "waiting" to waiting
        ))

        return OnboardingState(
            currentStep = OnboardingStep.fromInt(step),
            isWaitingForHomeSelection = waiting,
            isOnboardingCompleted = completed,
            isSetAsDefaultHome = false
        )
    }

    private fun updateState(newState: OnboardingState) {
        _state.value = newState
        AppLogger.state(TAG, "StateUpdate", mapOf(
            "step" to newState.currentStep,
            "waiting" to newState.isWaitingForHomeSelection,
            "completed" to newState.isOnboardingCompleted,
            "isDefault" to newState.isSetAsDefaultHome
        ))
    }

    private fun handleSideEffects(action: OnboardingAction, state: OnboardingState) {
        when {
            state.isWaitingForHomeSelection && action is OnboardingAction.ContinueClicked -> {
                AppLogger.d(TAG, "Triggering home app selection")
                _viewEvents.value = ViewEvent.OpenHomeAppSelection
            }
            state.shouldFinishActivity -> {
                AppLogger.d(TAG, "Finishing activity")
                _viewEvents.value = ViewEvent.FinishActivity
                repository.clearOnboardingData()
            }
        }
    }

    private fun persistState(state: OnboardingState) {
        repository.setCurrentStep(state.currentStep.stepNumber)
        repository.setWaitingForHomeSelection(state.isWaitingForHomeSelection)

        if (state.isOnboardingCompleted) {
            repository.setOnboardingCompleted(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        AppLogger.d(TAG, "OnboardingViewModel cleared")
    }
}

sealed class ViewEvent {
    data object OpenHomeAppSelection : ViewEvent()
    data object FinishActivity : ViewEvent()
}