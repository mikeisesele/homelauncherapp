package com.michael.homeapponboarding

import android.util.Log

object AppLogger {
    private const val APP_TAG = "HomeApp"
    private var isDebugMode = true // Set to false for release builds

    fun d(tag: String, message: String) {
        if (isDebugMode) {
            Log.d("$APP_TAG-$tag", message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebugMode) {
            if (throwable != null) {
                Log.e("$APP_TAG-$tag", message, throwable)
            } else {
                Log.e("$APP_TAG-$tag", message)
            }
        }
    }

    // State logging helper
    fun state(tag: String, state: String, values: Map<String, Any?>) {
        if (isDebugMode) {
            val details = values.entries.joinToString(", ") { "${it.key}=${it.value}" }
            Log.d("$APP_TAG-$tag", "$state: $details")
        }
    }
}