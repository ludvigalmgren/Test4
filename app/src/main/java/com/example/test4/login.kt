package com.example.test4

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Log.d
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit



class login : AppCompatActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth

    // [END declare_auth]

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.test4.R.layout.activity_login)

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        // [START on_start_check_user]
        buttonSendSMS.setOnClickListener{
            // Check if user is signed in (non-null) and update UI accordingly
            verificationInProgress = true
            val currentUser = auth.currentUser
            // [START_EXCLUDE]
            d("Ludvig","$verificationInProgress")
            if (verificationInProgress && validatePhoneNumber()) {
                startPhoneNumberVerification(editTextPhone.text.toString())
            }
            // [END_EXCLUDE]
        }
        // [END on_start_check_user]

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
            // [START verify_with_code]
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            // [END verify_with_code]
            signInWithPhoneAuthCredential(credential)
        }

        //Go back when Back button pushed
        buttonSubmitCode.setOnClickListener {
            verifyPhoneNumberWithCode("Hej!",editTextCode.text.toString())
            startActivity(Intent(this, Name::class.java))
        }

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                d("Ludvig", "onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential)
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    d("Ludvig", "Invalid phone number.")
                    // [END_EXCLUDE]
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                d("Ludvig", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

            }
        }
        // [END phone_auth_callbacks]
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        verificationInProgress = true
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    d("Ludvig", "signInWithCredential:success")

                    val user = task.result?.user
                    // [START_EXCLUDE]
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        d("Ludvig", "Invalid Code.")
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }
    // [END sign_in_with_phone]

    private fun signOut() {
        auth.signOut()
        updateUI(STATE_INITIALIZED)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    fun updateUI(
        uiState: Int,
        user: FirebaseUser? = auth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button

            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the

            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options

            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in


                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.smsCode != null) {
                        d("Ludvig", "SMS.")
                    } else {
                    }
                }
            }
            STATE_SIGNIN_FAILED ->
                // No-op, handled by sign-in check
                d("Ludvig", "Hej!")
            STATE_SIGNIN_SUCCESS -> {
            }
        } // Np-op, handled by sign-in check

        if (user == null) {
            // Signed out

        } else {
            // Signed in


        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = editTextPhone.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            d("Ludvig", "Invalid phone number.")
            return false
        }

        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

   override fun onClick(view: View) {
     //   when (view.id) {
            //   buttonSubmitNumber.setOnClickListener {
            //       startPhoneNumberVerification(editTextPhone.text.toString())
            //    }
            //         buttonSubmitCode .setOnClickListener {
            //          val code = editTextCode.text.toString()
            //           if (TextUtils.isEmpty(code)) {
            //                d("Ludvig", "Cannot be empty.")
            //              }
//
            //          verifyPhoneNumberWithCode(storedVerificationId, code)
            //      }
       // }
   }

    companion object {
        private const val TAG = "PhoneAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}
