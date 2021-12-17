package com.example.nutrihanjum.user.settings

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.ActivityUpdateProfileBinding
import com.example.nutrihanjum.databinding.LayoutPopupPasswordResetBinding
import com.example.nutrihanjum.util.DelayedTextWatcher

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var viewModel: UpdateProfileViewModel
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>

    private val colorValid = Color.BLACK
    private val colorInvalid = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(UpdateProfileViewModel::class.java)

        initAgeSpinner()
        addLiveDataObserver()
        addTextInputValidChecker()
        setActivityLauncher()

        if (savedInstanceState == null) {
            binding.layoutLoading.visibility = View.VISIBLE
            viewModel.getUserProfile()

            viewModel.user.observe(this) {
                initView()
                binding.layoutLoading.visibility = View.GONE
            }
        }
        else {
            initView()
        }

        setContentView(binding.root)
    }


    private fun addLiveDataObserver() {
        viewModel.updateValid.observe(this) {
            binding.btnUpdateProfile.isEnabled = it
        }

        viewModel.userNameValid.observe(this) {
            binding.edittextUserName.setTextColor(if (it) colorValid else colorInvalid)
        }

        viewModel.updated.observe(this) {
            if (it) {
                setResult(Activity.RESULT_OK)
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.network_failed), Toast.LENGTH_SHORT).show()
            }

            binding.btnLoading.visibility = View.GONE
            binding.btnUpdateProfile.visibility = View.VISIBLE
            binding.btnUpdateProfile.isClickable = true
        }
    }


    private fun addTextInputValidChecker() {
        binding.edittextUserName.addTextChangedListener(object: DelayedTextWatcher(this) {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkUserNameValid(editable.toString())
            }
        })
    }


    private fun setActivityLauncher() {
        cropImageLauncher = registerForActivityResult(CropImageContract()) {
            if (it.isSuccessful) {
                viewModel.userPhoto = it.uriContent
                Glide.with(this)
                    .load(viewModel.userPhoto)
                    .circleCrop()
                    .into(binding.imageviewProfileImage)
            }
        }
    }


    private fun initView() {
        viewModel.userPhoto?.let {
            Glide.with(this)
                .load(viewModel.userPhoto)
                .circleCrop()
                .into(binding.imageviewProfileImage)
        }

        if (!viewModel.isPassword) {
            binding.layoutPasswordReset.visibility = View.GONE
        }
        else {
            initPasswordReset()
        }

        binding.edittextUserName.setText(viewModel.userName)

        if (viewModel.userAge >= 0) {
            binding.spinnerAge.setSelection(viewModel.userAge)
        }

        if (viewModel.userGender.isNotEmpty()) {
            binding.radioGroupGender.check(
                if (viewModel.userGender == "남") binding.radioBtnMale.id
                else binding.radioBtnFemale.id
            )
        }

        setViewListener()
    }


    private fun initAgeSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.age_range,
            R.layout.item_age_spinner
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_age_dropdown)
            binding.spinnerAge.adapter = adapter
        }
    }


    private fun setViewListener() {

        binding.imageviewProfileImage.setOnClickListener {
            cropImageLauncher.launch(options {
                setGuidelines(CropImageView.Guidelines.ON)
                setCropShape(CropImageView.CropShape.OVAL)
                setAspectRatio(1,1)
                setActivityMenuIconColor(Color.BLACK)
                setFixAspectRatio(true)
            })
        }

        binding.btnUpdateProfile.setOnClickListener {
            viewModel.userAge = binding.spinnerAge.selectedItemPosition

            when (binding.radioGroupGender.checkedRadioButtonId) {
                binding.radioBtnMale.id -> viewModel.userGender = "남"
                binding.radioBtnFemale.id -> viewModel.userGender = "여"
            }

            viewModel.updateProfile(
                binding.edittextUserName.text.toString()
            )

            binding.btnLoading.visibility = View.VISIBLE
            binding.btnUpdateProfile.visibility = View.INVISIBLE
            it.isClickable = false
        }

        binding.btnBack.setOnClickListener { onBackPressed() }
    }


    private fun initPasswordReset() {
        val passwordResetBinding = LayoutPopupPasswordResetBinding.inflate(layoutInflater)
        val passwordResetDialog = Dialog(this)

        passwordResetDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        passwordResetDialog.setContentView(passwordResetBinding.root)

        passwordResetBinding.btnCancelSetPassword.setOnClickListener { passwordResetDialog.dismiss() }
        passwordResetBinding.btnSetPassword.setOnClickListener {
            viewModel.reAuthenticate(passwordResetBinding.edittextPrevPassword.text.toString())
            passwordResetBinding.layoutLoading.visibility = View.VISIBLE
        }

        passwordResetBinding.edittextNewPassword.addTextChangedListener(object: DelayedTextWatcher(this) {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkPasswordValid(editable.toString())
            }
        })

        passwordResetBinding.edittextCheckPassword.addTextChangedListener(object: DelayedTextWatcher(this) {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkPasswordSame(passwordResetBinding.edittextNewPassword.text.toString(), editable.toString())
            }
        })

        viewModel.passwordValid.observe(this) {
            passwordResetBinding.edittextNewPassword.setTextColor(if (it) colorValid else colorInvalid)
            viewModel.checkPasswordSame(
                passwordResetBinding.edittextNewPassword.text.toString(),
                passwordResetBinding.edittextCheckPassword.text.toString()
            )
        }

        viewModel.passwordCheck.observe(this) {
            passwordResetBinding.edittextCheckPassword.setTextColor(if (it) colorValid else colorInvalid)
        }

        viewModel.passwordUpdateValid.observe(this) { passwordResetBinding.btnSetPassword.isClickable = it }

        viewModel.reAuthResult.observe(this) {
            if (it) {
                viewModel.updatePassword(passwordResetBinding.edittextNewPassword.text.toString())
            }
            else {
                Toast.makeText(this, getString(R.string.re_auth_failed), Toast.LENGTH_SHORT).show()
                passwordResetBinding.layoutLoading.visibility = View.GONE
            }
        }

        viewModel.passwordUpdated.observe(this) {
            if (it) {
                Toast.makeText(this, getString(R.string.update_password_succeed), Toast.LENGTH_SHORT).show()
                passwordResetDialog.dismiss()
            }
            else {
                Toast.makeText(this, getString(R.string.update_password_failed), Toast.LENGTH_SHORT).show()
            }
            passwordResetBinding.layoutLoading.visibility = View.GONE
        }

        binding.btnPasswordReset.setOnClickListener { passwordResetDialog.show() }
    }
}