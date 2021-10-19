package com.example.nutrihanjum.user

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var loginViewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        loginViewModel.loginResult.observe(this) {
            if (it.first) {
                setResult(Activity.RESULT_OK)
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_LONG).show()
            }
        }

        initView()
    }

    private fun initView() {
        supportFragmentManager.beginTransaction()
            .replace(binding.root.id, LoginFragment())
            .commit()
    }

}