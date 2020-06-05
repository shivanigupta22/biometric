package com.example.biometric

interface BiometricContract {
    interface View {
        fun showFingerPrintSystemUI()
        fun showFingerCustomUI()
        fun showAuthFailError()
        fun showAuthSuccessScreen()
        fun showNoFingerprintError()
        fun showTryAgain()
        fun onBiometricCancelled()
        fun showThirtySecondsWaitingUI()
        fun showUnlockUsingDeviceCred()
        fun showAlternateLoginMethod()
        fun showNoFingerprintEnrolledUI()

    }

    interface Presenter {
        fun checkIfBiometricSupported(biometricUtils: BiometricUtils?)
        fun generateSecretKey1()
    }
}