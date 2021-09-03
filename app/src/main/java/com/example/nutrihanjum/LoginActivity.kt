package com.example.nutrihanjum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.nutrihanjum.databinding.ActivityLoginBinding
import com.example.nutrihanjum.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}