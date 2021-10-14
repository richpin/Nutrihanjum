package com.example.nutrihanjum

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.nutrihanjum.databinding.ActivityAddDiaryBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.ClipData
import android.os.Build
import android.view.View
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.viewmodel.DiaryViewModel

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDiaryBinding
    private lateinit var photoLauncher: ActivityResultLauncher<Uri>
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
        val content = intent.getSerializableExtra("content") as Pair<ContentDTO, String>

        with(binding) {
            btnRegisterDiary.text = getString(R.string.modify_diary)
            edittextDiaryMemo.setText(content.first.content)
            radioGroupMealTime.check(mapMealTimeStringToId(content.first.mealTime))
            switchPublic.isChecked = content.first.isPublic
            Glide.with(this@AddDiaryActivity)
                .load(content.first.imageUrl)
                .into(imageviewPreview)


            binding.btnRegisterDiary.setOnClickListener {
                val selectedMealTime = mapMealTimeIdToString(binding.radioGroupMealTime.checkedRadioButtonId)

                it.isClickable = false

                with(content.first) {
                    this.content = edittextDiaryMemo.text.toString()
                    mealTime = selectedMealTime!!
                    isPublic = switchPublic.isChecked
                    timestamp = System.currentTimeMillis()

                    viewModel.modifyDiary(
                        content.first, content.second,
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

        photoLauncher = registerForActivityResult(TakePictureContract()) {
            if (it) {
                binding.imageviewPreview.setImageURI(photoURI)
                isPhotoExist = isPhotoExist or true
            } else {
                isPhotoExist = isPhotoExist or false
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
        if (photoURI == null) {
            val photoFile = createImageFile()

            photoFile?.also {
                photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
                photoLauncher.launch(photoURI)
            }
        } else {
            photoLauncher.launch(photoURI)
        }

    }

    private fun createImageFile(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return try {
            File.createTempFile("DIARY_${timestamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            null
        }
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
        getString(R.string.meal_time_dinner) ->  R.id.radio_btn_dinner
        getString(R.string.meal_time_dinner) -> R.id.radio_btn_snack
        getString(R.string.meal_time_midnight_snack) -> R.id.radio_btn_midnight
        else -> View.NO_ID
    }


    inner class TakePictureContract: ActivityResultContracts.TakePicture() {
        override fun createIntent(context: Context, input: Uri): Intent {
            val takePictureIntent = super.createIntent(context, input)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.clipData = ClipData.newRawUri("", photoURI)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            return takePictureIntent
        }
    }

}