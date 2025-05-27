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
 * EXERCISE REQUIREMENT: Step 2 - Set Default Fragment
 * Shows guidance text: "To continue, set the app as your default home"
 * When clicking continue button, open system UI to change default home app
 * Handle returning without setting as default vs setting as default
 */
class SetDefaultFragment : Fragment() {

    companion object {
        private const val TAG = "SetDefaultFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Creating set default fragment view")
        return inflater.inflate(R.layout.fragment_set_default, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up set default fragment")

        // EXERCISE REQUIREMENT: Continue button opens system UI for home app selection
        val continueButton = view.findViewById<Button>(R.id.btn_set_default)
        continueButton?.setOnClickListener {
            Log.d(TAG, "onViewCreated: Continue button clicked - processing MVI action for home selection")
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

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: SetDefaultFragment resumed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Set default fragment view destroyed")
    }
}