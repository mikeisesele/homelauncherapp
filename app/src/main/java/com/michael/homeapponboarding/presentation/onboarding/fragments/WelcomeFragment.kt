package com.michael.homeapponboarding.presentation.onboarding.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.michael.homeapponboarding.presentation.onboarding.OnboardingActivity
import com.michael.homeapponboarding.R

/**
 * EXERCISE REQUIREMENT: Step 1 - Welcome Fragment
 * Shows welcome text and continue button
 * When clicking continue button, go to next step
 */
class WelcomeFragment : Fragment() {

    companion object {
        private const val TAG = "WelcomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Creating welcome fragment view")
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up welcome fragment")

        // EXERCISE REQUIREMENT: Continue button to go to next step
        val continueButton = view.findViewById<Button>(R.id.btn_continue)
        continueButton?.setOnClickListener {
            Log.d(TAG, "onViewCreated: Continue button clicked - processing MVI action")
            (activity as? OnboardingActivity)?.let { onboardingActivity ->
                if (!onboardingActivity.isFinishing && !onboardingActivity.isDestroyed) {
                    onboardingActivity.proceedToNextStep()
                } else {
                    Log.w(TAG, "onViewCreated: Activity is finishing/destroyed, ignoring click")
                }
            } ?: run {
                Log.e(TAG, "onViewCreated: Activity is null or not OnboardingActivity")
            }
        } ?: run {
            Log.e(TAG, "onViewCreated: Continue button not found in layout")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Welcome fragment view destroyed")
    }
}