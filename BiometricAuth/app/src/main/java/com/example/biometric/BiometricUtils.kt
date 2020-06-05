package com.example.biometric

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

sealed class BiometricUtils {
    abstract val hasBiometrics: Boolean
    abstract val isSystemPromptSupported: Boolean
    abstract val isBiometricEnrolled: Boolean

    @TargetApi(Build.VERSION_CODES.Q)
    private class QBiometricChecker(
        private val biometricManager: BiometricManager
    ) : BiometricUtils() {

        companion object {
            fun getInstance(context: Context): QBiometricChecker? =
                QBiometricChecker(BiometricManager.from(context))
        }

        private val availableCodes = listOf(
            BiometricManager.BIOMETRIC_SUCCESS,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        )

        override val hasBiometrics: Boolean
            get() = availableCodes.contains(biometricManager.canAuthenticate())
        override val isSystemPromptSupported: Boolean = true
        override val isBiometricEnrolled: Boolean
            get() = !(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED == biometricManager.canAuthenticate())
    }

    @Suppress("DEPRECATION")
    private class LegacyBiometricChecker(
        private val fingerprintManager: FingerprintManagerCompat
    ) : BiometricUtils() {

        companion object {
            fun getInstance(context: Context): LegacyBiometricChecker? =
                LegacyBiometricChecker(FingerprintManagerCompat.from(context))
        }

        override val hasBiometrics: Boolean
            get() = fingerprintManager.isHardwareDetected
        override val isSystemPromptSupported: Boolean
            get() = hasBiometrics && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        override val isBiometricEnrolled: Boolean
            get() = fingerprintManager.hasEnrolledFingerprints()
    }

    private class DefaultBiometricChecker : BiometricUtils() {
        override val hasBiometrics: Boolean = false
        override val isSystemPromptSupported: Boolean = false
        override val isBiometricEnrolled: Boolean = false
    }

    companion object {
        fun getInstance(context: Context): BiometricUtils? {
            return when {
                Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ->
                    QBiometricChecker.getInstance(context)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    LegacyBiometricChecker.getInstance(context)
                else -> null
            } ?: DefaultBiometricChecker()
        }
    }

}