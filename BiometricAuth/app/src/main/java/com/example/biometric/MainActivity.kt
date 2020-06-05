package com.example.biometric

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyProperties
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey

class MainActivity : FragmentActivity(), BiometricContract.View {
    private var biometricPrompt: BiometricPrompt? = null
    private var secretKey: SecretKey? = null
    private lateinit var mCallback: BiometricPrompt.AuthenticationCallback
    private var mBioPresenter: BiometricContract.Presenter? = null
    private var biometricUtils: BiometricUtils? = null
    private var cipher: Cipher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBioPresenter = BiometricPresenter(this)
        biometricUtils = BiometricUtils.getInstance(this)
        mCallback = mBioPresenter as BiometricPrompt.AuthenticationCallback
       if(biometricUtils?.hasBiometrics == true) {
           if(biometricUtils?.isBiometricEnrolled == true) {
               mBioPresenter?.generateSecretKey1()
               createBiometricPrompt()
           } else {
               showNoFingerprintEnrolledUI()
           }
       }
        mBioPresenter?.checkIfBiometricSupported(biometricUtils)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun showFingerPrintSystemUI() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login Using Biometric")
            .setSubtitle("Scan your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()
        cipher?.let { BiometricPrompt.CryptoObject(it) }?.let {
            biometricPrompt?.authenticate(promptInfo, it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createBiometricPrompt() {
        cipher = getCipher()
        secretKey = getSecretKey()
        cipher?.init(Cipher.ENCRYPT_MODE, secretKey)
        biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), mCallback)
    }

    override fun showFingerCustomUI() {
    }

    override fun showAuthFailError() {
    }

    override fun showAuthSuccessScreen() {
        createDialog("Successful login")
    }

    override fun showTryAgain() {
        createDialog("Try Again")
    }

    override fun onBiometricCancelled() {
        createDialog("Cancelled")
    }

    override fun showThirtySecondsWaitingUI() {
        createDialog("Wait for 30sec")
    }

    override fun showUnlockUsingDeviceCred() {
        createDialog("Use Device cred to login because of too many wait 30 sec attempts")
    }

    override fun showAlternateLoginMethod() {
        createDialog("Login using another method")
    }

    override fun showNoFingerprintEnrolledUI() {
        createDialog("Please enroll fingerprint first")
    }

    override fun showNoFingerprintError() {
        createDialog("No fingerprint hardware")
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.getKey("key1", null) as SecretKey
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    private fun createDialog(text: String) {
        val dialogB = AlertDialog.Builder(this)
        dialogB.setTitle(text)
        val dialog = dialogB.create()
        dialog.setButton(Dialog.BUTTON_POSITIVE, "ok") {
                _, _ -> dialog.dismiss()
        }
        dialog.show()

    }

    fun onAuthClick(view: View) {
        if(biometricUtils?.isBiometricEnrolled == true) {
            mBioPresenter?.generateSecretKey1()
            createBiometricPrompt()
            mBioPresenter?.checkIfBiometricSupported(biometricUtils)
        } else {
            showNoFingerprintEnrolledUI()
        }
    }
}
