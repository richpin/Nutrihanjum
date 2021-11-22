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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.canhub.cropper.*
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
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

        initFoodRecyclerView()
        initAutoCompleteRecyclerview()
    }


    private fun initFoodRecyclerView() {
        val adapter = FoodRecyclerViewAdapter(viewModel.foodList.value!!)
        binding.recyclerviewFoods.adapter = adapter
        binding.recyclerviewFoods.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)

        adapter.revokeListener = { binding.layoutFoodDetail.root.visibility = View.GONE }
        adapter.textChangeListener = { name ->
            synchronized(this) {
                viewModel.loadFoodAutoComplete(name)
            }
        }

        adapter.addFoodListener = { food, pos ->
            with (binding.layoutFoodDetail) {
                edittextFoodDetailName.setText(food.name)
                edittextFoodDetailCalorie.setText(food.calorie)
                edittextFoodDetailCarbohydrate.setText(food.carbohydrate)
                edittextFoodDetailProtein.setText(food.protein)
                edittextFoodDetailFat.setText(food.fat)

                when {
                    food.name.isEmpty() -> { edittextFoodDetailName.requestFocus() }
                    food.calorie.isEmpty() -> { edittextFoodDetailCalorie.requestFocus() }
                    food.carbohydrate.isEmpty() -> { edittextFoodDetailCarbohydrate.requestFocus() }
                    food.protein.isEmpty() -> { edittextFoodDetailProtein.requestFocus() }
                    else -> { edittextFoodDetailFat.requestFocus() }
                }
            }
            binding.layoutFoodDetail.root.visibility = View.VISIBLE
            viewModel.workingPosition = pos
            viewModel.workingItem = food
        }

        binding.layoutFoodDetail.btnSetFood.setOnClickListener {
            viewModel.workingItem?.let { food ->
                with (binding.layoutFoodDetail) {
                    food.name = edittextFoodDetailName.text.toString()
                    food.calorie = edittextFoodDetailCalorie.text.toString()
                    food.carbohydrate = edittextFoodDetailCarbohydrate.text.toString()
                    food.protein = edittextFoodDetailProtein.text.toString()
                    food.fat = edittextFoodDetailFat.text.toString()
                }

                binding.layoutFoodDetail.root.visibility = View.GONE

                adapter.notifyItemChanged(viewModel.workingPosition)
                viewModel.workingItem = null
            }
        }

        binding.layoutFoodDetail.btnCancelSetFood.setOnClickListener {
            binding.layoutFoodDetail.root.visibility = View.GONE
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initAutoCompleteRecyclerview() {
        val adapter = AutoCompleteAdapter(viewModel.foodAutoComplete.value!!)
        binding.recyclerviewAutocomplete.adapter = adapter
        binding.recyclerviewAutocomplete.layoutManager = LinearLayoutManager(this)

        viewModel.foodAutoComplete.observe(this) {
            binding.recyclerviewAutocomplete.adapter?.notifyDataSetChanged()
        }

        adapter.itemSelectedListener = { food ->
            viewModel.foodList.value?.add(food)
            binding.recyclerviewFoods.adapter!!.notifyItemInserted(viewModel.foodList.value!!.size - 1)

            with (binding.layoutFoodDetail) {
                edittextFoodDetailName.setText(food.name)
                edittextFoodDetailCalorie.setText(food.calorie)
                edittextFoodDetailCarbohydrate.setText(food.carbohydrate)
                edittextFoodDetailProtein.setText(food.protein)
                edittextFoodDetailFat.setText(food.fat)

                when {
                    food.name.isEmpty() -> { edittextFoodDetailName.requestFocus() }
                    food.calorie.isEmpty() -> { edittextFoodDetailCalorie.requestFocus() }
                    food.carbohydrate.isEmpty() -> { edittextFoodDetailCarbohydrate.requestFocus() }
                    food.protein.isEmpty() -> { edittextFoodDetailProtein.requestFocus() }
                    else -> { edittextFoodDetailFat.requestFocus() }
                }
            }
            binding.layoutFoodDetail.root.visibility = View.VISIBLE
            viewModel.workingPosition = viewModel.foodList.value!!.size - 1
            viewModel.workingItem = food
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