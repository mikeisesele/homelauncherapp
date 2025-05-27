package com.michael.homeapponboarding.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.michael.homeapponboarding.R
import com.michael.homeapponboarding.presentation.onboarding.fragments.SetDefaultFragment
import com.michael.homeapponboarding.presentation.onboarding.fragments.ThankYouFragment
import com.michael.homeapponboarding.presentation.onboarding.fragments.WelcomeFragment
import com.michael.homeapponboarding.presentation.onboarding.mvi.OnboardingState
import com.michael.homeapponboarding.presentation.onboarding.mvi.OnboardingStep
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Onboarding activity that guides user through setting up the app as default home
 *
 * EXERCISE REQUIREMENTS:
 * - 3 steps: Welcome, Set Default, Thank You
 * - User can only go forward, not backward
 * - Handle edge cases: configuration changes, process death, external home app changes
 *
 * ARCHITECTURE:
 * - Uses MVI (Model-View-Intent) pattern with unidirectional data flow
 * - StateFlow for reactive state management
 * - ViewEvents for one-time UI actions
 * - Pure reducer functions for predictable state transitions
 * - Dependency Injection with Hilt
 *
 * MVI FLOW:
 * User Action → OnboardingAction → ViewModel.processAction() →
 * OnboardingReducer.reduce() → New State → UI Update
 */
@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OnboardingActivity"
    }

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewModel.markOnboardingStarted()

        // Update context with package name BEFORE setting up observers
        viewModel.updateContextWithPackageName(packageName)

        setupObservers()
        viewModel.initialize()
    }

    /**
     * Set up observers for MVI State and ViewEvents
     * StateFlow for continuous state updates
     * LiveData for one-time view events
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                showStep(state.currentStep)
            }
        }

        viewModel.viewEvents.observe(this) { event ->
            handleViewEvent(event)
        }
    }

    /**
     * Handle one-time view events that don't belong in state
     * These are actions that should happen once and not be repeated on state restoration
     */
    private fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.OpenHomeAppSelection -> {
                Log.d(TAG, "handleViewEvent: Opening home app selection")
                openHomeAppSelection()
            }

            is ViewEvent.FinishActivity -> {
                Log.d(TAG, "handleViewEvent: Finishing activity")
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Update context and process activity resume through MVI action
        viewModel.updateContextWithPackageName(packageName)
        viewModel.onActivityResumed()
    }

    /**
     * Show the appropriate fragment based on current step
     * Called reactively when state.currentStep changes
     */
    private fun showStep(step: OnboardingStep) {
        Log.d(TAG, "showStep: Showing step $step")

        val fragment: Fragment = when (step) {
            OnboardingStep.Welcome -> {
                Log.d(TAG, "showStep: Creating WelcomeFragment")
                WelcomeFragment()
            }
            OnboardingStep.SetDefault -> {
                Log.d(TAG, "showStep: Creating SetDefaultFragment")
                SetDefaultFragment()
            }
            OnboardingStep.ThankYou -> {
                Log.d(TAG, "showStep: Creating ThankYouFragment")
                ThankYouFragment()
            }
        }

        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            Log.d(TAG, "showStep: Successfully replaced fragment for step $step")
        } catch (e: Exception) {
            Log.e(TAG, "showStep: Error showing fragment for step $step", e)
        }
    }

    /**
     * Called by fragments when user clicks continue button
     * Processes the action through MVI flow
     */
    fun proceedToNextStep() {
        Log.d(TAG, "proceedToNextStep: Processing continue action through MVI")
        viewModel.onContinueClicked()
    }

    /**
     * Open system home app selection UI
     * Called as side effect when ViewEvent.OpenHomeAppSelection is received
     */
    private fun openHomeAppSelection() {
        Log.d(TAG, "openHomeAppSelection: Opening system home app selection")
        try {
            Log.d(TAG, "openHomeAppSelection: About to try opening settings instead of home intent")

            openDefaultAppsSettings()

        } catch (e: Exception) {
            Log.e(TAG, "openHomeAppSelection: Error opening home selection", e)

            // Show error dialog with manual instructions
            showManualSetupDialog()
        }
    }

    /**
     * Show manual setup dialog when automatic methods fail
     */
    private fun showManualSetupDialog() {
        if (isFinishing || isDestroyed) return

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Set as Default Home")
            .setMessage("Please set this app as your default home manually:\n\n" +
                    "1. Go to Settings\n" +
                    "2. Find Apps or Application Manager\n" +
                    "3. Look for Default apps or Default applications\n" +
                    "4. Select Home app or Launcher\n" +
                    "5. Choose this app from the list\n" +
                    "6. Return to this app")
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                try {
                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "showManualSetupDialog: Error opening settings", e)
                }
            }
            .setNegativeButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Open default apps settings directly
     */
    private fun openDefaultAppsSettings() {
        try {
            // Try to open default apps settings directly
            val settingsIntent = Intent().apply {
                action = android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
            }

            if (settingsIntent.resolveActivity(packageManager) != null) {
                Log.d(TAG, "openDefaultAppsSettings: Opening default apps settings")
                startActivity(settingsIntent)
            } else {
                // Fallback to general settings
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "openDefaultAppsSettings: Error opening settings", e)
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: New intent received with action: ${intent.action}")

        // Update the intent
        setIntent(intent)

        // Update context and process new intent through MVI action
        viewModel.updateContextWithPackageName(packageName)
        viewModel.onNewIntent()
    }

    /**
     * EXERCISE REQUIREMENT: User can only go forward, not backward
     * Override back press to prevent backward navigation in onboarding flow
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: Back press ignored - onboarding only allows forward navigation")
        // Do nothing - prevent going back in onboarding flow
        // This maintains the exercise requirement for forward-only navigation
    }
}