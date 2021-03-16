package com.dtako.freecall.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dtako.freecall.R
import com.dtako.freecall.model.User
import com.dtako.freecall.utils.Constants
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class LoginActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var loginButton: LoginButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setProgressBar(findViewById(R.id.progress_bar))
        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()
        loginButton = findViewById(R.id.login_button)
        loginButton.setPermissions("email", "public_profile")
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(applicationContext, error!!.message.toString(), Toast.LENGTH_SHORT).show()
            }

        })
    }

    @SuppressLint("CommitPrefEdits")
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
        if (currentUser != null) {
            transition()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        // [START_EXCLUDE silent]
        showProgressBar()
        // [END_EXCLUDE]

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        updateUI(user)
                        registerUser()

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    hideProgressBar()
                    // [END_EXCLUDE]
                }
    }

    @SuppressLint("CommitPrefEdits")
    private fun registerUser() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { it ->
            val id = Firebase.auth.currentUser!!.uid
            val tokenU = it.result!!
            val status = true
            FirebaseFirestore.getInstance().collection(Constants.USER)
                    .document(id)
                    .get()
                    .addOnCompleteListener {
                        if (!it.result!!.exists()) {
                            val user = User(id, tokenU, status)
                            FirebaseFirestore.getInstance().collection(Constants.USER)
                                    .document(id)
                                    .set(user)
                                    .addOnCompleteListener {
                                        transition()
                                    }
                        } else {
                            transition()
                        }
                    }

        }
    }

    private fun transition() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            loginButton.visibility = View.INVISIBLE
        } else {
            loginButton.visibility = View.VISIBLE
        }
    }

}