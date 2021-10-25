package com.example.nutrihanjum.user

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.databinding.UserFragmentBinding
import com.example.nutrihanjum.user.login.LoginActivity

class UserFragment: Fragment() {
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

    private lateinit var userViewModel : UserViewModel

    private lateinit var loginLauncher: ActivityResultLauncher<Intent>
    private lateinit var profileLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentBinding.inflate(layoutInflater)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        makeActivityLauncher()
        addViewListener()
        addLiveDataObserver()

        return binding.root
    }


    private fun makeActivityLauncher() {
        loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                userViewModel.notifyUserSigned()
            }
        }

        profileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                userViewModel.photoUrl?.let {
                    Glide.with(this)
                        .load(it)
                        .circleCrop()
                        .into(binding.imageviewUserPhoto)
                }
                binding.textviewUserId.text = userViewModel.userName
            }
        }
    }


    private fun addViewListener() {
        binding.btnLogout.setOnClickListener {
            userViewModel.signOut(requireContext())
            userViewModel.notifyUserSignedOut()
        }

        binding.layoutProfileSignedOut.setOnClickListener {
            loginLauncher.launch(Intent(activity, LoginActivity::class.java))
        }

        binding.btnUpdateProfile.setOnClickListener {
            profileLauncher.launch(Intent(activity, UpdateProfileActivity::class.java))
        }
    }


    private fun addLiveDataObserver() {
        userViewModel.signed.observe(viewLifecycleOwner) { signed ->
            if (signed) {
                updateForSignIn()
            }
            else {
                updateForSignOut()
            }
        }
    }




    private fun updateForSignIn() {
        binding.textviewUserId.text = userViewModel.userName

        userViewModel.photoUrl?.let {
            Glide.with(this)
                .load(userViewModel.photoUrl)
                .circleCrop()
                .into(binding.imageviewUserPhoto)
        }

        binding.layoutProfileSigned.visibility = View.VISIBLE
        binding.layoutProfileSignedOut.visibility = View.GONE
        binding.layoutSetting.visibility = View.VISIBLE
    }


    private fun updateForSignOut() {
        binding.textviewUserId.text = getString(R.string.request_login)

        binding.layoutProfileSigned.visibility = View.GONE
        binding.layoutProfileSignedOut.visibility = View.VISIBLE
        binding.layoutSetting.visibility = View.GONE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}