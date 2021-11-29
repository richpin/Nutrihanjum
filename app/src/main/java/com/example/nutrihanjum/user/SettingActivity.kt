package com.example.nutrihanjum.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.nutrihanjum.MainActivity
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.databinding.ActivitySettingBinding
import com.example.nutrihanjum.util.NHUtil

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    private lateinit var userViewModel : UserViewModel

    private lateinit var profileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        makeActivityLauncher()
        addViewListener()

        setContentView(binding.root)
    }

    private fun makeActivityLauncher() {
        profileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val intent = Intent().apply { putExtra("setting", NHUtil.Setting.PROFILE_EDIT)}
                setResult(Activity.RESULT_OK, intent)
            }
        }
    }

    private fun addViewListener(){
        binding.btnRequestLogout.setOnClickListener {
            userViewModel.signOut(it.context)
            userViewModel.notifyUserSignedOut()

            val intent = Intent().apply{ putExtra("setting", NHUtil.Setting.LOG_OUT) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        binding.btnProfileEdit.setOnClickListener {
            profileLauncher.launch(Intent(this, UpdateProfileActivity::class.java))
        }

        binding.settingActivityBackButton.setOnClickListener { onBackPressed() }
    }
}