package com.example.nutrihanjum.user.settings

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.R
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.databinding.ActivitySettingBinding
import com.example.nutrihanjum.databinding.LayoutPopupPasswordCheckBinding
import com.example.nutrihanjum.user.login.LoginActivity
import com.example.nutrihanjum.util.NHUtil

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    private lateinit var userViewModel : UserViewModel

    private lateinit var profileLauncher: ActivityResultLauncher<Intent>

    private lateinit var passwordCheckBinding: LayoutPopupPasswordCheckBinding
    private lateinit var passwordCheckDialog: Dialog

    private val preference by lazy {
        getPreferences(Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        makeActivityLauncher()
        addLiveDataObserver()
        addViewListener()
        makeDialog()

        userViewModel.setNoticeFlag(preference.getBoolean("notice", true))

        if (savedInstanceState == null) {
            userViewModel.getNoticeFlag()
        }

        setContentView(binding.root)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun addLiveDataObserver() {
        userViewModel.noticeFlag.observe(this) {
            preference.edit().apply {
                putBoolean("notice", it)
                apply()
            }
            binding.switchNotice.isChecked = it
        }

        userViewModel.userDeleteResult.observe(this) {
            passwordCheckBinding.layoutLoading.visibility = View.GONE

            when (it) {
                NHUtil.WithdrawResult.SUCCESS -> {
                    val intent = Intent().apply { putExtra("setting", NHUtil.Setting.WITHDRAW) }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                NHUtil.WithdrawResult.RE_AUTHENTICATE_NEEDED -> {
                    userViewModel.signOut(this)

                    val intent = Intent().apply { putExtra("setting", NHUtil.Setting.LOG_OUT) }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                else -> {
                    Toast.makeText(this, getString(R.string.network_failed), Toast.LENGTH_SHORT).show()
                    passwordCheckDialog.dismiss()
                }
            }
        }
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

            val intent = Intent().apply{ putExtra("setting", NHUtil.Setting.LOG_OUT) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        binding.btnProfileEdit.setOnClickListener {
            profileLauncher.launch(Intent(this, UpdateProfileActivity::class.java))
        }

        binding.settingActivityBackButton.setOnClickListener { onBackPressed() }

        binding.switchNotice.setOnClickListener {
            userViewModel.updateNoticeFlag(binding.switchNotice.isChecked)
        }

        binding.btnLateVersion.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("market://details?id=" + this.getString(R.string.app_package_name))
            ContextCompat.startActivity(this, intent, null)
        }

        binding.btnRequestWithdrawal.setOnClickListener {
            passwordCheckDialog.show()
        }
    }


    private fun makeDialog() {
        passwordCheckBinding = LayoutPopupPasswordCheckBinding.inflate(layoutInflater)
        passwordCheckDialog = Dialog(this@SettingActivity)

        passwordCheckDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        passwordCheckDialog.setContentView(passwordCheckBinding.root)

        passwordCheckBinding.btnPositive.setOnClickListener {
            userViewModel.removeUser()
        }


        passwordCheckBinding.btnNegative.setOnClickListener { passwordCheckDialog.dismiss() }
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}