package com.example.nutrihanjum

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
import androidx.activity.viewModels
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.viewmodel.DiaryViewModel

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDiaryBinding
    private lateinit var photoLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null
    private lateinit var date: String

    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        date = intent.getSerializableExtra("date") as String

        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoLauncher = registerForActivityResult(TakePictureContract()) {
            if (it) {
                binding.imageviewPreview.setImageURI(photoURI)
            }
        }

        binding.imageviewPreview.setOnClickListener {
            getPhoto()
        }

        binding.btnRegisterDiary.setOnClickListener {
            val selectedMealTime = getMealTime()

            if (selectedMealTime == null) {
                Toast.makeText(this, getString(R.string.msg_mealtime_not_selected), Toast.LENGTH_LONG).show()
            }
            else if (photoURI == null) {
                Toast.makeText(this, getString(R.string.msg_photo_not_selected), Toast.LENGTH_LONG).show()
            }
            else {
                val content = ContentDTO()

                with(binding) {
                    content.content = edittextDiaryMemo.text.toString()
                    content.mealTime = selectedMealTime
                    content.imageUrl = photoURI.toString()
                    content.isPublic = switchPublic.isChecked
                    content.timestamp = System.currentTimeMillis()
                    content.date = date

                    viewModel.setDiary(content)
                }
            }
        }
        viewModel.diarySetResult.observe(this) {
            if (it) {
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show()
            }
        }

        getPhoto()
    }

    private fun getPhoto() {
        val photoFile = createImageFile()

        photoFile?.also {
            photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
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

    private fun getMealTime() = when (binding.radioGroupMealTime.checkedRadioButtonId) {
        R.id.radio_btn_breakfast -> getString(R.string.meal_time_breakfast)
        R.id.radio_btn_lunch -> getString(R.string.meal_time_lunch)
        R.id.radio_btn_dinner -> getString(R.string.meal_time_dinner)
        R.id.radio_btn_snack -> getString(R.string.meal_time_dinner)
        R.id.radio_btn_midnight -> getString(R.string.meal_time_midnight_snack)
        else -> null
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