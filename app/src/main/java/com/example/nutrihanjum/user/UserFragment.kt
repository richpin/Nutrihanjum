package com.example.nutrihanjum.user

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.nutrihanjum.MainActivity
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.databinding.UserFragmentBinding
import com.example.nutrihanjum.repository.UserRepository.openKakaoChannel
import com.example.nutrihanjum.user.login.LoginActivity
import com.example.nutrihanjum.util.NHUtil
import com.example.nutrihanjum.util.NHUtil.Setting.LOG_OUT
import com.example.nutrihanjum.util.NHUtil.Setting.PROFILE_EDIT

class UserFragment: Fragment() {

    private var _binding : UserFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel : UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentBinding.inflate(layoutInflater)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        setActivityLauncher()
        setProfile()
        addViewListener()

        return binding.root
    }

    private fun setProfile(){
        userViewModel.photoUrl?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.imageviewUserPhoto)
        }

        binding.textviewUserId.text = userViewModel.userName
    }

    private fun setActivityLauncher() {
        (activity as MainActivity).settingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val action = result.data?.getSerializableExtra("setting") as NHUtil.Setting

                when(action){
                    PROFILE_EDIT -> {
                        setProfile()
                    }
                    LOG_OUT -> {
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun addViewListener() {
        binding.btnUserSavedPost.setOnClickListener {
            startActivity(Intent(activity, SavedContentActivity::class.java))
        }

        binding.btnUserMyPost.setOnClickListener {
            startActivity(Intent(activity, MyContentActivity::class.java))
        }

        binding.btnUserAnnouncement.setOnClickListener {
            val intent = Intent(requireContext(), PostActivity::class.java)
            intent.putExtra("isFaq", false)
            startActivity(intent)
        }

        binding.btnUserFaq.setOnClickListener {
            val intent = Intent(requireContext(), PostActivity::class.java)
            intent.putExtra("isFaq", true)
            startActivity(intent)
        }

        binding.btnUserKakaochannel.setOnClickListener {
            openKakaoChannel(requireContext())
        }
    }
}