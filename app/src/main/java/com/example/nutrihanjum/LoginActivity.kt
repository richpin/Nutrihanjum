package com.example.nutrihanjum

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.databinding.ActivityLoginBinding
import com.example.nutrihanjum.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel : LoginViewModel by viewModels()

    private lateinit var googleLoginLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        setLoginListener()

        loginViewModel.loginResult.observe(this) {
            if (it.first) {
                setResult(Activity.RESULT_OK)
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun googleLogin() {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this@LoginActivity, gso)
        googleLoginLauncher.launch(client.signInIntent)
    }

    private fun setLoginListener() {

        googleLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val credential = GoogleAuthProvider.getCredential(task.result.idToken, null)
                loginViewModel.authWithCredential(credential)
            }
            catch(e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }

        binding.btnGoogleLogin.setOnClickListener {
            googleLogin()
        }
    }

}