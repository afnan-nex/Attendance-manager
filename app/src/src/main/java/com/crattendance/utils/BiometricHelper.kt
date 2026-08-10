package com.crattendance.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/** Thin wrapper around the AndroidX BiometricPrompt API. */
object BiometricHelper {

    sealed class Status {
        object Success : Status()
        object NoHardware : Status()
        object NoEnrolled : Status()
        object HwUnavailable : Status()
        object Unsupported : Status()
    }

    /** Whether the device can perform strong biometrics or device-credential auth. */
    fun status(context: Context): Status = when (
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    ) {
        BiometricManager.BIOMETRIC_SUCCESS -> Status.Success
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Status.NoHardware
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Status.NoEnrolled
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Status.HwUnavailable
        else -> Status.Unsupported
    }

    /**
     * Shows the system biometric prompt. DEVICE_CREDENTIAL (PIN/pattern/password)
     * is always allowed as a fallback so the app can never be permanently locked.
     */
    fun showPrompt(
        activity: FragmentActivity,
        title: CharSequence,
        subtitle: CharSequence? = null,
        description: CharSequence? = null,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> }
    ) {
        val executor: Executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString)
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                if (subtitle != null) setSubtitle(subtitle)
                if (description != null) setDescription(description)
            }
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(promptInfo)
    }
}
