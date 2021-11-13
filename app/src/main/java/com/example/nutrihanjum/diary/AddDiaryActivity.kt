package com.example.nutrihanjum.diary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.nutrihanjum.databinding.ActivityAddDiaryBinding
import java.util.*
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.canhub.cropper.*
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDiaryBinding
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>

    private var photoURI: Uri? = null
    private var isPhotoExist = false

    private lateinit var viewModel: DiaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
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

        addListenerForAddDairy(date)
        addLiveDataObserverForAddDairy()
    }


    private fun addListenerForAddDairy(date: String) {
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


    private fun addLiveDataObserverForAddDairy() {
        viewModel.diary.observe(this) {
            if (it != null) {
                val mIntent = Intent()
                mIntent.putExtra("addedContent", it)
                setResult(Activity.RESULT_OK, mIntent)
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show()
                binding.btnRegisterDiary.isClickable = true
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
        }

        addListenerForModifyDairy(content)
        addLiveDataObserverForModifyDairy()
    }


    private fun addListenerForModifyDairy(content: ContentDTO) {
        binding.btnRegisterDiary.setOnClickListener {
            val selectedMealTime = mapMealTimeIdToString(binding.radioGroupMealTime.checkedRadioButtonId)

            it.isClickable = false

            with(content) {
                this.content = binding.edittextDiaryMemo.text.toString()
                mealTime = selectedMealTime!!
                isPublic = binding.switchPublic.isChecked
                timestamp = System.currentTimeMillis()

                viewModel.modifyDiary(
                    this,
                    if (isPhotoExist) photoURI.toString() else null
                )
            }
        }
    }


    private fun addLiveDataObserverForModifyDairy() {
        viewModel.diary.observe(this) {
            if (it != null) {
                val mIntent = Intent()
                mIntent.putExtra("modifiedContent", it)
                setResult(Activity.RESULT_OK, mIntent)
                finish()
            }
            else {
                Toast.makeText(this, getString(R.string.failed_to_upload), Toast.LENGTH_SHORT).show()
                binding.btnRegisterDiary.isClickable = true
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initCommonView() {
        binding.recyclerviewFoods.adapter = AutoCompleteAdapter(viewModel.foodList.value!!)
        binding.recyclerviewFoods.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)

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

        binding.btnAddFood.setOnClickListener {
            if (binding.edittextFood.visibility == View.VISIBLE) {
                if (binding.edittextFood.text.isNotEmpty()) {
                    viewModel.loadFoodList(binding.edittextFood.text.toString())
                }

                binding.edittextFood.setText("")
                binding.edittextFood.visibility = View.GONE
            } else {
                binding.edittextFood.visibility = View.VISIBLE
            }
        }

        viewModel.foodList.observe(this) {
            binding.recyclerviewFoods.adapter?.notifyDataSetChanged()
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
        R.id.radio_btn_snack -> getString(R.string.meal_time_snack)
        R.id.radio_btn_midnight -> getString(R.string.meal_time_midnight_snack)
        else -> null
    }

    private fun mapMealTimeStringToId(mealtime: String) = when(mealtime) {
        getString(R.string.meal_time_breakfast) -> R.id.radio_btn_breakfast
        getString(R.string.meal_time_lunch) -> R.id.radio_btn_lunch
        getString(R.string.meal_time_dinner) -> R.id.radio_btn_dinner
        getString(R.string.meal_time_snack) -> R.id.radio_btn_snack
        getString(R.string.meal_time_midnight_snack) -> R.id.radio_btn_midnight
        else -> View.NO_ID
    }

}