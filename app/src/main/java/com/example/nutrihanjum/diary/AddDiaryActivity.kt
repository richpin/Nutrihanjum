package com.example.nutrihanjum.diary

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.nutrihanjum.databinding.ActivityAddDiaryBinding
import java.util.*
import android.view.View
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.canhub.cropper.*
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDiaryBinding
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>

    private var photoURI: Uri? = null
    private var isPhotoExist = false

    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCommonView()

        if (intent.hasExtra("date")) {
            initForAddDiary()
            getPhoto()
        } else {
            initForModifyDiary()
        }
    }

    private fun initForAddDiary() {
        val date = intent.getStringExtra("date")!!

        binding.btnRegisterDiary.text = getString(R.string.add_diary)

        binding.btnRegisterDiary.setOnClickListener {
            val selectedMealTime = mapMealTimeIdToString(binding.radioGroupMealTime.checkedRadioButtonId)

            if (!isPhotoExist) {
                Toast.makeText(this, getString(R.string.msg_photo_not_selected), Toast.LENGTH_LONG).show()
            }
            else if (selectedMealTime == null) {
                Toast.makeText(this, getString(R.string.msg_mealtime_not_selected), Toast.LENGTH_LONG).show()
            }
            else {
                it.isClickable = false

                val content = ContentDTO()

                with(binding) {
                    content.content = edittextDiaryMemo.text.toString()
                    content.mealTime = selectedMealTime
                    content.isPublic = switchPublic.isChecked
                    content.timestamp = System.currentTimeMillis()
                    content.date = date

                    viewModel.addDiary(content, photoURI.toString())
                }
            }
        }
    }

    private fun initForModifyDiary() {
        val content = intent.getSerializableExtra("content") as ContentDTO

        with(binding) {
            btnRegisterDiary.text = getString(R.string.modify_diary)
            edittextDiaryMemo.setText(content.content)
            radioGroupMealTime.check(mapMealTimeStringToId(content.mealTime))
            switchPublic.isChecked = content.isPublic
            Glide.with(this@AddDiaryActivity)
                .load(content.imageUrl)
                .into(imageviewPreview)


            binding.btnRegisterDiary.setOnClickListener {
                val selectedMealTime = mapMealTimeIdToString(binding.radioGroupMealTime.checkedRadioButtonId)

                it.isClickable = false

                with(content) {
                    this.content = edittextDiaryMemo.text.toString()
                    mealTime = selectedMealTime!!
                    isPublic = switchPublic.isChecked
                    timestamp = System.currentTimeMillis()

                    viewModel.modifyDiary(
                        content,
                        if (isPhotoExist) photoURI.toString() else null
                    )
                }


            }
        }
    }

    private fun initCommonView() {
        binding.imageviewPreview.setOnClickListener {
            getPhoto()
        }

        cropImageLauncher = registerForActivityResult(CropImageContract()) {
            if (it.isSuccessful) {
                photoURI = it.uriContent
                binding.imageviewPreview.setImageURI(photoURI)
                isPhotoExist = true
            }
        }

        viewModel.diaryChanged.observe(this) {
            if (it) {
                setResult(Activity.RESULT_OK)
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show()
                binding.btnRegisterDiary.isClickable = true
            }

        }
    }

    private fun getPhoto() {
        cropImageLauncher.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1,1)
                setFixAspectRatio(true)
            }
        )
    }

    private fun mapMealTimeIdToString(mealtime: Int) = when (mealtime) {
        R.id.radio_btn_breakfast -> getString(R.string.meal_time_breakfast)
        R.id.radio_btn_lunch -> getString(R.string.meal_time_lunch)
        R.id.radio_btn_dinner -> getString(R.string.meal_time_dinner)
        R.id.radio_btn_snack -> getString(R.string.meal_time_dinner)
        R.id.radio_btn_midnight -> getString(R.string.meal_time_midnight_snack)
        else -> null
    }

    private fun mapMealTimeStringToId(mealtime: String) = when(mealtime) {
        getString(R.string.meal_time_breakfast) -> R.id.radio_btn_breakfast
        getString(R.string.meal_time_lunch) -> R.id.radio_btn_lunch
        getString(R.string.meal_time_dinner) -> R.id.radio_btn_dinner
        getString(R.string.meal_time_dinner) -> R.id.radio_btn_snack
        getString(R.string.meal_time_midnight_snack) -> R.id.radio_btn_midnight
        else -> View.NO_ID
    }

}