package com.example.nutrihanjum

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.databinding.ActivityLoginBinding
import com.example.nutrihanjum.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var loginViewModel : LoginViewModel

    private lateinit var googleLoginLauncher : ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLoginListener()

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginViewModel.loginResult.observe(this) {
            if (it.first) {
                finish()
            }
            else {
                Toast.makeText(this, it.second, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun googleLogin() {
        googleLoginLauncher.launch(getString(R.string.web_client_id))
    }

    private fun setLoginListener() {
        googleLoginLauncher = registerForActivityResult(GoogleLoginIntentContract()) {
            it?.let{ loginViewModel.authWithGoogle(it) }
        }

        binding.btnGoogleLogin.setOnClickListener {
            googleLogin()
        }
    }

    inner class GoogleLoginIntentContract : ActivityResultContract<String, AuthCredential?>() {
        private lateinit var client : GoogleSignInClient
        override fun createIntent(context: Context, clientID: String): Intent {
            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientID)
                .requestEmail()
                .build()

            client = GoogleSignIn.getClient(this@LoginActivity, gso)

            return client.signInIntent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): AuthCredential? {
            return when (resultCode) {
                Activity.RESULT_OK -> getCredential(intent)
                else -> null
            }
        }

        private fun getCredential(intent : Intent?) : AuthCredential? {
            return try {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent!!)!!
                GoogleAuthProvider.getCredential(result.signInAccount!!.idToken, null)
            } catch(e : Error) {
                Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                null
            }
        }

    }
}