package com.example.nutrihanjum.user

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.nutrihanjum.databinding.ActivityUpdateProfileBinding
import com.example.nutrihanjum.util.DelayedTextWatcher

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var viewModel: UpdateProfileViewModel
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>

    private var photoURI: Uri? = null

    private val colorValid = Color.BLACK
    private val colorInvalid = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(UpdateProfileViewModel::class.java)

        initView()
        addLiveDataObserver()
        addTextInputValidChecker()
        setActivityLauncher()
    }


    private fun addLiveDataObserver() {
        viewModel.updateValid.observe(this) {
            binding.btnUpdateProfile.isEnabled = it
        }

        viewModel.emailValid.observe(this) {
            binding.edittextEmail.setTextColor(if (it) colorValid else colorInvalid)
        }

        viewModel.userNameValid.observe(this) {
            binding.edittextUserName.setTextColor(if (it) colorValid else colorInvalid)
        }

        viewModel.updated.observe(this) {
            if (it) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }


    private fun addTextInputValidChecker() {
        binding.edittextUserName.addTextChangedListener(object: DelayedTextWatcher(this) {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkUserNameValid(editable.toString())
            }
        })

        binding.edittextEmail.addTextChangedListener(object: DelayedTextWatcher(this) {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkEmailValid(editable.toString())
            }
        })
    }


    private fun setActivityLauncher() {
        cropImageLauncher = registerForActivityResult(CropImageContract()) {
            if (it.isSuccessful) {
                photoURI = it.uriContent
                Glide.with(this)
                    .load(photoURI)
                    .circleCrop()
                    .into(binding.imageviewProfileImage)
            }
        }
    }


    private fun initView() {
        viewModel.userPhoto?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.imageviewProfileImage)
        }

        binding.edittextEmail.setText(viewModel.userEmail)
        binding.edittextUserName.setText(viewModel.userName)
        binding.imageviewProfileImage.setOnClickListener {
            cropImageLauncher.launch(options {
                setGuidelines(CropImageView.Guidelines.ON)
                setCropShape(CropImageView.CropShape.OVAL)
                setAspectRatio(1,1)
                setFixAspectRatio(true)
            })
        }

        binding.btnUpdateProfile.setOnClickListener {
            viewModel.updateProfile(
                binding.edittextEmail.text.toString(),
                binding.edittextUserName.text.toString(),
                photoURI
            )

            it.isClickable = false
        }
    }
}