package com.michael.homeapponboarding.presentation.onboarding.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.michael.homeapponboarding.R
import com.michael.homeapponboarding.presentation.onboarding.OnboardingViewModel

/**
 * EXERCISE REQUIREMENT: Step 3 - Thank You Fragment
 * Shows thank you text: "Thanks for setting as default home"
 * When clicking continue button, finish the onboarding activity
 */
class ThankYouFragment : Fragment() {

    companion object {
        private const val TAG = "ThankYouFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Creating thank you fragment view")
        return inflater.inflate(R.layout.fragment_thank_you, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up thank you fragment")

        // EXERCISE REQUIREMENT: Continue button finishes onboarding activity
        val continueButton = view.findViewById<Button>(R.id.btn_finish)
        continueButton?.setOnClickListener {
             requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ThankYouFragment resumed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Thank you fragment view destroyed")
    }
}