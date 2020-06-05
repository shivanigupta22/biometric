package com.example.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricConstants.*
import androidx.biometric.BiometricPrompt
import java.nio.charset.Charset
import java.util.*
import javax.crypto.KeyGenerator

class BiometricPresenter(var view: BiometricContract.View) : BiometricContract.Presenter,
    BiometricPrompt.AuthenticationCallback() {

    override fun checkIfBiometricSupported(biometricUtils: BiometricUtils?) {
        biometricUtils?.let {
            when {
                it.hasBiometrics -> {
                    if (it.isSystemPromptSupported) view.showFingerPrintSystemUI()
                    else view.showFingerPrintSystemUI()
                }
                else -> view.showNoFingerprintError()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        when(errorCode) {
            ERROR_HW_UNAVAILABLE or ERROR_UNABLE_TO_PROCESS -> {
                view.showTryAgain()
            }
            ERROR_CANCELED or ERROR_NEGATIVE_BUTTON -> {
                view.onBiometricCancelled()
            }
            ERROR_HW_NOT_PRESENT -> {
                view.showNoFingerprintError()
            }
            ERROR_LOCKOUT or ERROR_TIMEOUT -> {
                view.showThirtySecondsWaitingUI()
            }
            ERROR_LOCKOUT_PERMANENT -> {
                view.showUnlockUsingDeviceCred()
            }
            ERROR_USER_CANCELED or ERROR_NEGATIVE_BUTTON -> {
                view.showAlternateLoginMethod()
            }
            ERROR_NO_BIOMETRICS -> {
                view.showNoFingerprintEnrolledUI()
            }

        }
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        view.showAuthFailError()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        val encryptedInfo: ByteArray? = result.cryptoObject?.cipher?.doFinal("string".toByteArray(
            Charset.defaultCharset()))
        Log.d("MY_APP_TAG", "Encrypted information: " + Arrays.toString(encryptedInfo))
        view.showAuthSuccessScreen()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun generateSecretKey1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            generateSecretKey(
                KeyGenParameterSpec.Builder(
                    "key1",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    // Invalidate the keys if the user has registered a new biometric
                    // credential, such as a new fingerprint. Can call this method only
                    // on Android 7.0 (API level 24) or higher. The variable
                    // "invalidatedByBiometricEnrollment" is true by default.
                    .setInvalidatedByBiometricEnrollment(true)
                    .build()
            )
        } else {
            generateSecretKey(
                KeyGenParameterSpec.Builder(
                    "key1",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .build()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
}