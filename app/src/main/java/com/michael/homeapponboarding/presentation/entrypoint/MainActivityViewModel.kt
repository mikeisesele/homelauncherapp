package com.michael.homeapponboarding.presentation.entrypoint

import android.util.Log
import androidx.lifecycle.ViewModel
import com.michael.homeapponboarding.data.HomeAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Simple ViewModel for MainActivity that provides access to SharedPreferences via Repository
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: HomeAppRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MainActivityViewModel"
    }

    /**
     * Check if onboarding is completed
     */
    fun isOnboardingCompleted(): Boolean {
        return repository.isOnboardingCompleted()
    }

    /**
     * Check if app is set as default home
     */
    fun isSetAsDefaultHome(packageName: String): Boolean {
        return repository.isSetAsDefaultHome(packageName)
    }

    /**
     * Clear onboarding completion flag (for state inconsistency)
     */
    fun clearOnboardingCompletion() {
        Log.w(TAG, "clearOnboardingCompletion: Clearing onboarding completion flag")
        repository.setOnboardingCompleted(false)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "MainActivityViewModel cleared")
    }
}