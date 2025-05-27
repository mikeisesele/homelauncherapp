package com.michael.homeapponboarding.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.michael.homeapponboarding.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeAppRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val packageManager: PackageManager,
    private val context: Context
) {
    companion object {
        private const val TAG = "Repository"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_CURRENT_STEP = "current_step"
        const val KEY_WAITING_FOR_HOME_SELECTION = "waiting_for_home_selection"
        const val KEY_ONBOARDING_STARTED = "onboarding_started"
        const val KEY_INSTALLATION_TIMESTAMP = "installation_timestamp"
    }

    fun isTrulyFreshInstall(): Boolean {
        val isFreshInstallation = isFreshInstallation()
        val hasStartedOnboarding = sharedPreferences.getBoolean(KEY_ONBOARDING_STARTED, false)

        AppLogger.state(TAG, "FreshCheck", mapOf(
            "hasStarted" to hasStartedOnboarding,
            "isFresh" to isFreshInstallation
        ))

        // If installation is fresh, it's truly fresh regardless of started flag
        // The started flag might be restored from backup
        return if (isFreshInstallation) {
            AppLogger.d(TAG, "Fresh installation detected - clearing started flag")
            // Clear the started flag since this is truly fresh
            sharedPreferences.edit()
                .putBoolean(KEY_ONBOARDING_STARTED, false)
                .apply()
            true
        } else {
            // If not fresh installation, check if onboarding was started
            !hasStartedOnboarding
        }
    }

    /**
     * Enhanced onboarding completion check
     */
    fun isOnboardingCompleted(): Boolean {
        if (isTrulyFreshInstall()) {
            AppLogger.d(TAG, "Fresh install detected - setting up")
            handleFreshInstallation()
            return false
        }

        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Get current step with validation
     */
    fun getCurrentStep(): Int {
        if (isTrulyFreshInstall()) {
            return 1 // Always Welcome for fresh installs
        }

        return sharedPreferences.getInt(KEY_CURRENT_STEP, 1)
    }

    /**
     * Check if app is set as default home
     */
    fun isSetAsDefaultHome(packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }

            val resolveInfo: ResolveInfo? = packageManager.resolveActivity(intent, 0)
            val currentDefault = resolveInfo?.activityInfo?.packageName
            val isDefault = currentDefault == packageName

            AppLogger.state(TAG, "DefaultCheck", mapOf(
                "ourPackage" to packageName,
                "currentDefault" to currentDefault,
                "isDefault" to isDefault
            ))

            isDefault
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error checking default home", e)
            false
        }
    }

    /**
     * Mark onboarding as started
     */
    fun markOnboardingStarted() {
        sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_STARTED, true)
            .apply()
        AppLogger.d(TAG, "Onboarding marked as started")
    }

    /**
     * Set onboarding completion
     */
    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .apply()
        AppLogger.d(TAG, "Onboarding completed: $completed")
    }

    /**
     * Set current step
     */
    fun setCurrentStep(step: Int) {
        sharedPreferences.edit()
            .putInt(KEY_CURRENT_STEP, step)
            .apply()
        AppLogger.d(TAG, "Step set to: $step")
    }

    /**
     * Get/Set waiting for home selection
     */
    fun isWaitingForHomeSelection(): Boolean {
        return sharedPreferences.getBoolean(KEY_WAITING_FOR_HOME_SELECTION, false)
    }

    fun setWaitingForHomeSelection(waiting: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_WAITING_FOR_HOME_SELECTION, waiting)
            .apply()
        AppLogger.d(TAG, "Waiting for selection: $waiting")
    }

    /**
     * Clear all data
     */
    private fun clearAllData() {
        sharedPreferences.edit().clear().apply()
        AppLogger.d(TAG, "All data cleared")
    }

    fun clearOnboardingData() {
        sharedPreferences.edit()
            .remove(KEY_ONBOARDING_COMPLETED)
            .remove(KEY_CURRENT_STEP)
            .remove(KEY_WAITING_FOR_HOME_SELECTION)
            .apply()
        AppLogger.d(TAG, "Onboarding data cleared")
    }

    // Private helper methods
    private fun isFreshInstallation(): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val actualInstallTime = packageInfo.firstInstallTime
            val savedInstallTime = sharedPreferences.getLong(KEY_INSTALLATION_TIMESTAMP, -1L)

            val noSavedTime = savedInstallTime == -1L
            val timestampMismatch = savedInstallTime != actualInstallTime
            val recentInstall = (System.currentTimeMillis() - actualInstallTime) < (10 * 60 * 1000)

            val isFresh = noSavedTime || timestampMismatch || recentInstall

            AppLogger.state(TAG, "InstallCheck", mapOf(
                "noSavedTime" to noSavedTime,
                "timestampMismatch" to timestampMismatch,
                "recentInstall" to recentInstall,
                "isFresh" to isFresh
            ))

            isFresh
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error checking installation", e)
            true
        }
    }

    private fun handleFreshInstallation() {
        clearAllData()
        recordCurrentInstallation()
        AppLogger.d(TAG, "Fresh installation setup complete")
    }

    private fun recordCurrentInstallation() {
        try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val installTime = packageInfo.firstInstallTime

            sharedPreferences.edit()
                .putLong(KEY_INSTALLATION_TIMESTAMP, installTime)
                .apply()

            AppLogger.d(TAG, "Installation recorded: $installTime")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error recording installation", e)
        }
    }
}