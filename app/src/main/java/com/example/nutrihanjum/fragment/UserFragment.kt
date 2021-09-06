package com.example.nutrihanjum.fragment

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.example.nutrihanjum.LoginActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.UserFragmentBinding
import com.example.nutrihanjum.viewmodel.UserViewModel

class UserFragment private constructor() : Fragment() {
    companion object {
        @Volatile private var instance: UserFragment? = null

        @JvmStatic fun getInstance(): UserFragment = instance ?: synchronized(this) {
            instance ?: UserFragment().also {
                instance = it
            }
        }
    }
    private var _binding : UserFragmentBinding? = null
    private val binding get() = _binding!!
    private val userViewModel : UserViewModel by activityViewModels()
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = UserFragmentBinding.inflate(layoutInflater)

        loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                userViewModel.userSigned()
            }
        }

        binding.btnLogin.setOnClickListener {
            loginLauncher.launch(Intent(activity, LoginActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            userViewModel.userSignedOut()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.signed.observe(viewLifecycleOwner) { signed ->
            if (signed) {
                binding.textviewUserEmail.text = userViewModel.userEmail
            }
            else {
                binding.textviewUserEmail.text = "로그인 해주세요!!"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}