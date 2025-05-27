

package com.michael.homeapponboarding.presentation.entrypoint

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.michael.homeapponboarding.R
import com.michael.homeapponboarding.presentation.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main home activity that serves as the default launcher
 * This activity will be shown when user presses home button
 * Now uses ViewModel to access SharedPreferences through Repository
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Inject ViewModel using Hilt
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: MainActivity started")
        Log.d(TAG, "onCreate: Intent action: ${intent.action}, categories: ${intent.categories}")

        // Check if onboarding was completed
        val onboardingCompleted = viewModel.isOnboardingCompleted()
        val isCurrentlyDefaultHome = viewModel.isSetAsDefaultHome(packageName)

        // Always redirect to onboarding immediately without setting content view
        if (!onboardingCompleted) {
            Log.d(TAG, "onCreate: Onboarding not completed - redirecting to onboarding (fresh install or incomplete)")
            startOnboardingActivityImmediately()
            return
        }

        // Check for state inconsistency (completed but not default home)
        if (onboardingCompleted && !isCurrentlyDefaultHome) {
            Log.w(TAG, "onCreate: INCONSISTENT STATE - completed but not default home")
            Log.w(TAG, "onCreate: Clearing completion flag and restarting onboarding")

            viewModel.clearOnboardingCompletion()
            startOnboardingActivityImmediately()
            return
        }

        // Only reach here if onboarding completed AND we're still default home
        Log.d(TAG, "onCreate: Normal home screen display - onboarding completed and we're default")
        setContentView(R.layout.activity_main)
    }

    private fun startOnboardingActivityImmediately() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish() // Immediately finish MainActivity so user never sees it
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: MainActivity resumed")

        // Double-check onboarding status on resume
        val onboardingCompleted = viewModel.isOnboardingCompleted()
        val isCurrentlyDefaultHome = viewModel.isSetAsDefaultHome(packageName)

        // Check for state inconsistency in onResume as well
        if (onboardingCompleted && !isCurrentlyDefaultHome) {
            Log.w(TAG, "onResume: State inconsistency detected - clearing completion flag")
            viewModel.clearOnboardingCompletion()
            startOnboardingActivity()
            return
        }

        if (!onboardingCompleted) {
            Log.d(TAG, "onResume: Onboarding still not completed, redirecting")
            startOnboardingActivity()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: New intent received")

        // Set the new intent
        setIntent(intent)

        // Handle new intents (e.g., when home button is pressed again)
        val onboardingCompleted = viewModel.isOnboardingCompleted()
        val isCurrentlyDefaultHome = viewModel.isSetAsDefaultHome(packageName)

        // Check for state inconsistency with new intent as well
        if (onboardingCompleted && !isCurrentlyDefaultHome) {
            Log.w(TAG, "onNewIntent: State inconsistency detected - clearing completion flag")
            viewModel.clearOnboardingCompletion()
            startOnboardingActivity()
            return
        }

        if (!onboardingCompleted) {
            Log.d(TAG, "onNewIntent: Onboarding not completed, redirecting")
            startOnboardingActivity()
        }
    }

    private fun startOnboardingActivity() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish() // Finish MainActivity to prevent multiple instances
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // EXERCISE REQUIREMENT: Home activity should not respond to back press
        Log.d(TAG, "onBackPressed: Back press ignored in home activity")
    }
}
