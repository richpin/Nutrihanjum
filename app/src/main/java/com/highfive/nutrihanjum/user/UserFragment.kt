package com.example.nutrihanjum.user

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.nutrihanjum.MainActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.databinding.UserFragmentBinding
import com.example.nutrihanjum.repository.UserRepository.openKakaoChannel
import com.example.nutrihanjum.user.login.LoginActivity
import com.example.nutrihanjum.util.NHUtil
import com.example.nutrihanjum.util.NHUtil.Setting.*
import com.example.nutrihanjum.util.ProgressBarAnimationUtil.setProgressWithAnimation

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
        initProfile()
        initNutritionInfo()
        addViewListener()

        if (savedInstanceState == null) {
            userViewModel.getUserProfile()
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        userViewModel.getTodayNutrition()
    }



    private fun initProfile() {
        val ageList = requireActivity().resources.getStringArray(R.array.age_range)
        val ageNotSelected = getString(R.string.age_not_selected)

        userViewModel.user.observe(viewLifecycleOwner) {
            userViewModel.photoUrl?.let {
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(binding.imageviewUserPhoto)
            }

            binding.textviewUserId.text = userViewModel.userName

            var userInfo = ""

            userInfo += if (it.age == 0) ageNotSelected else ageList[it.age]
            if (it.gender.isNotEmpty()) userInfo += " ${it.gender}자"

            binding.textviewUserInfo.text = userInfo
        }
    }


    private fun initNutritionInfo() {
        binding.edittextFoodName.inputType = InputType.TYPE_NULL
        binding.edittextCalorie.inputType = InputType.TYPE_NULL
        binding.edittextCarbohydrate.inputType = InputType.TYPE_NULL
        binding.edittextProtein.inputType = InputType.TYPE_NULL
        binding.edittextFat.inputType = InputType.TYPE_NULL

        binding.edittextFoodName.setSelectAllOnFocus(false)
        binding.edittextCalorie.setSelectAllOnFocus(false)
        binding.edittextCarbohydrate.setSelectAllOnFocus(false)
        binding.edittextProtein.setSelectAllOnFocus(false)
        binding.edittextFat.setSelectAllOnFocus(false)

        userViewModel.nutritionInfo.observe(viewLifecycleOwner) {
            val total = it.carbohydrate + it.protein + it.fat

            binding.edittextCalorie.setText(it.calorie.toString())
            binding.edittextCarbohydrate.setText(it.carbohydrate.toString())
            binding.edittextProtein.setText(it.protein.toString())
            binding.edittextFat.setText(it.fat.toString())

            binding.progressBarCarbohydrate.setProgressWithAnimation((it.carbohydrate / total * 1000).toInt())
            binding.progressBarProtein.setProgressWithAnimation((it.protein / total * 1000).toInt())
            binding.progressBarFat.setProgressWithAnimation((it.fat / total * 1000).toInt())
        }
    }



    private fun setActivityLauncher() {
        (activity as MainActivity).settingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val action = result.data?.getSerializableExtra("setting") as NHUtil.Setting

                when(action){
                    PROFILE_EDIT -> {
                        userViewModel.getUserProfile()
                    }
                    LOG_OUT -> {
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                    WITHDRAW -> {
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