package com.example.nutrihanjum.diary

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.nutrihanjum.databinding.ActivityAddDiaryBinding
import java.util.*
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.canhub.cropper.*
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.FoodDTO
import com.example.nutrihanjum.model.NutritionInfo
import com.example.nutrihanjum.util.DelayedTextWatcher
import com.example.nutrihanjum.util.ProgressBarAnimationUtil.setProgressWithAnimation
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlin.math.max

class AddDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDiaryBinding
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>

    private lateinit var viewModel: DiaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        setContentView(binding.root)

        initCommonView()

        if (intent.hasExtra("date")) {
            val date = intent.getIntExtra("date", 0)

            initForAddDiary()
            addListenerForAddDairy(date)
            addLiveDataObserverForAddDairy()

            if (viewModel.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(viewModel.photoUrl)
                    .into(binding.imageviewPreview)
            }

            if (savedInstanceState == null) {
                getPhoto()
            }
        }
        else {
            if (savedInstanceState == null) {
                viewModel.content = intent.getSerializableExtra("content") as ContentDTO
                viewModel.loadDiaryById(viewModel.content.id)
                binding.layoutLoading.visibility = View.VISIBLE
            }
            else if (viewModel.photoUrl.isNotEmpty())  {
                viewModel.content.imageUrl = viewModel.photoUrl
            }

            initForModifyDiary()
            addListenerForModifyDairy()
            addLiveDataObserverForModifyDairy()
        }
    }


    private fun initForAddDiary() {
        binding.btnRegisterDiary.text = getString(R.string.add_diary)
        binding.textviewTitle.text = getString(R.string.title_add_diary)
    }


    private fun addListenerForAddDairy(date: Int) {
        binding.btnRegisterDiary.setOnClickListener {
            val selectedMealTime = mapMealTimeIdToString(binding.radioGroupMealTime.checkedRadioButtonId)

            if (viewModel.photoUrl.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_photo_not_selected), Toast.LENGTH_LONG).show()
            }
            else if (selectedMealTime == null) {
                Toast.makeText(this, getString(R.string.msg_mealtime_not_selected), Toast.LENGTH_LONG).show()
            }
            else {
                it.isClickable = false
                it.visibility = View.INVISIBLE
                binding.btnLoading.visibility = View.VISIBLE

                val content = ContentDTO()

                with(binding) {
                    content.content = edittextDiaryMemo.text.toString()
                    content.mealTime = selectedMealTime
                    content.isPublic = switchPublic.isChecked
                    content.timestamp = System.currentTimeMillis()
                    content.date = date
                    content.foods = viewModel.foodList
                    content.foods.forEach {
                        content.nutritionInfo.calorie += it.calorie
                        content.nutritionInfo.carbohydrate += it.carbohydrate
                        content.nutritionInfo.protein += it.protein
                        content.nutritionInfo.fat += it.fat
                    }
                    content.hashTagList = binding.edittextHashtag.hashTagList

                    viewModel.addDiary(content)
                }
            }
        }
    }


    private fun addLiveDataObserverForAddDairy() {
        viewModel.diaryResult.observe(this) {
            binding.btnRegisterDiary.isClickable = true
            binding.btnRegisterDiary.visibility = View.VISIBLE
            binding.btnLoading.visibility = View.GONE

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


    private fun initForModifyDiary() = synchronized(this) {
        val content = viewModel.content

        with(binding) {
            btnRegisterDiary.text = getString(R.string.modify_diary)
            edittextDiaryMemo.setText(content.content)
            radioGroupMealTime.check(mapMealTimeStringToId(content.mealTime))
            switchPublic.isChecked = content.isPublic

            Glide.with(this@AddDiaryActivity)
                .load(content.imageUrl)
                .into(imageviewPreview)

            viewModel.foodList.clear()
            viewModel.foodList.addAll(content.foods)
            binding.recyclerviewFoods.adapter?.notifyDataSetChanged()
            edittextHashtag.hashTagList = content.hashTagList
        }
    }


    private fun addListenerForModifyDairy() {
        val content = viewModel.content

        binding.btnRegisterDiary.setOnClickListener {
            val selectedMealTime = mapMealTimeIdToString(binding.radioGroupMealTime.checkedRadioButtonId)

            it.isClickable = false
            it.visibility = View.INVISIBLE
            binding.btnLoading.visibility = View.VISIBLE

            with(content) {
                this.content = binding.edittextDiaryMemo.text.toString()
                mealTime = selectedMealTime!!
                isPublic = binding.switchPublic.isChecked
                foods = viewModel.foodList
                nutritionInfo = NutritionInfo()
                foods.forEach {
                    nutritionInfo.calorie += it.calorie
                    nutritionInfo.carbohydrate += it.carbohydrate
                    nutritionInfo.protein += it.protein
                    nutritionInfo.fat += it.fat
                }
                content.hashTagList = binding.edittextHashtag.hashTagList

                viewModel.modifyDiary(this)
            }
        }
    }


    private fun addLiveDataObserverForModifyDairy() {
        viewModel.diaryResult.observe(this) {
            binding.btnRegisterDiary.isClickable = true
            binding.btnRegisterDiary.visibility = View.VISIBLE
            binding.btnLoading.visibility = View.GONE

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

        viewModel.diary.observe(this) {
            binding.layoutLoading.visibility = View.GONE

            if (it == null) {
                Toast.makeText(this, "이미 삭제된 일지입니다.", Toast.LENGTH_SHORT).show()

                val mIntent = Intent()
                mIntent.putExtra("deletedContent", viewModel.content)
                setResult(Activity.RESULT_OK, mIntent)
                finish()
            }
            else if (it != viewModel.content) {
                viewModel.content = it
                initForModifyDiary()
            }
        }
    }


    private fun initCommonView() {
        binding.imageviewPreview.setOnClickListener {
            getPhoto()
        }

        cropImageLauncher = registerForActivityResult(CropImageContract()) {
            if (it.isSuccessful) {
                viewModel.photoUrl = it.uriContent.toString()
                binding.imageviewPreview.setImageURI(it.uriContent)
            }
        }

        initFoodRecyclerView()
        initAutoCompleteText()

        val adapter = binding.recyclerviewFoods.adapter as FoodRecyclerViewAdapter

        binding.layoutFoodDetail.layoutSetBtn.visibility = View.VISIBLE

        binding.layoutFoodDetail.btnSetFood.setOnClickListener {
            viewModel.workingItem?.let { food ->
                with (binding.layoutFoodDetail) {
                    food.name = edittextFoodName.text.toString()
                    food.calorie = edittextCalorie.text.toString().toFloatOrNull() ?: 0f
                    food.carbohydrate = edittextCarbohydrate.text.toString().toFloatOrNull() ?: 0f
                    food.protein = edittextProtein.text.toString().toFloatOrNull() ?: 0f
                    food.fat = edittextFat.text.toString().toFloatOrNull() ?: 0f
                }

                binding.layoutFoodDetail.root.visibility = View.GONE

                adapter.notifyItemChanged(viewModel.workingPosition)
                viewModel.workingItem = null
            }
        }

        binding.layoutFoodDetail.btnCancelSetFood.setOnClickListener {
            binding.layoutFoodDetail.root.visibility = View.GONE
            viewModel.workingItem = null
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.layoutFoodDetail.lineDivision.visibility = View.GONE
    }


    private fun initAutoCompleteText() {

        val adapter = AutoCompleteAdapter(this, arrayListOf())

        binding.autoCompleteTextFood.setAdapter(adapter)
        binding.autoCompleteTextFood.threshold = 1

        binding.autoCompleteTextFood.setOnItemClickListener { adapterView, _, i, _ ->
            with (binding.recyclerviewFoods.adapter as FoodRecyclerViewAdapter) {
                val item = adapterView.getItemAtPosition(i) as FoodDTO

                foodList.add(item)
                notifyItemInserted(itemCount - 1)
                binding.autoCompleteTextFood.setText("")
                binding.layoutFoodDetail.root.visibility = View.VISIBLE
                foodSetListener?.invoke(item, itemCount - 1)
            }
        }

        binding.autoCompleteTextFood.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                with (binding.recyclerviewFoods.adapter as FoodRecyclerViewAdapter) {
                    val item = FoodDTO(textView.text.toString())

                    foodList.add(FoodDTO(textView.text.toString()))
                    notifyItemInserted(itemCount - 1)
                    binding.autoCompleteTextFood.setText("")
                    binding.layoutFoodDetail.root.visibility = View.VISIBLE
                    foodSetListener?.invoke(item, itemCount - 1)
                }

                return@setOnEditorActionListener true
            }

            false
        }

        binding.autoCompleteTextFood.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.layoutFoodDetail.root.visibility = View.GONE
            }
        }

        binding.autoCompleteTextFood.addTextChangedListener(object: DelayedTextWatcher(this) {
            override fun delayedOnTextChanged(text: CharSequence?) {
                if (!binding.autoCompleteTextFood.isPerformingCompletion && binding.autoCompleteTextFood.hasFocus()) {
                    viewModel.loadFoodAutoComplete(text.toString())
                }
            }
        })

        viewModel.foodAutoComplete.observe(this) {
            adapter.foodList.clear()
            adapter.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }



    private var isAnimated = true

    private fun initFoodRecyclerView() {
        val adapter = FoodRecyclerViewAdapter(viewModel.foodList)

        binding.recyclerviewFoods.adapter = adapter
        binding.recyclerviewFoods.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)

        adapter.foodSetListener = { food, pos ->
            with (binding.layoutFoodDetail) {
                isAnimated = false
                edittextFoodName.setText(food.name)
                edittextCalorie.setText(food.calorie.toString())
                edittextCarbohydrate.setText(food.carbohydrate.toString())
                edittextProtein.setText(food.protein.toString())
                edittextFat.setText(food.fat.toString())
                isAnimated = true

                val total = max(food.carbohydrate + food.protein + food.fat, 1f)
                progressBarCarbohydrate.setProgressWithAnimation((food.carbohydrate / total * 1000).toInt())
                progressBarProtein.setProgressWithAnimation((food.protein / total * 1000).toInt())
                progressBarFat.setProgressWithAnimation((food.fat / total * 1000).toInt())

                when {
                    food.name.isEmpty() -> { edittextFoodName.clearFocus(); edittextFoodName.requestFocus() }
                    food.calorie == 0f -> { edittextCalorie.clearFocus(); edittextCalorie.requestFocus() }
                    food.carbohydrate == 0f -> { edittextCarbohydrate.clearFocus(); edittextCarbohydrate.requestFocus() }
                    food.protein == 0f -> { edittextProtein.clearFocus(); edittextProtein.requestFocus() }
                    else -> { edittextFat.clearFocus(); edittextFat.requestFocus() }
                }
            }
            binding.layoutFoodDetail.root.visibility = View.VISIBLE
            viewModel.workingPosition = pos
            viewModel.workingItem = food
        }

        addProgressbarLiveUpdate()
    }


    private fun addProgressbarLiveUpdate() {
        binding.layoutFoodDetail.edittextCarbohydrate.addTextChangedListener {
            if (!isAnimated) return@addTextChangedListener

            val carbohydrate = binding.layoutFoodDetail.edittextCarbohydrate.text.toString().toFloatOrNull() ?: 0f
            val protein = binding.layoutFoodDetail.edittextProtein.text.toString().toFloatOrNull() ?: 0f
            val fat = binding.layoutFoodDetail.edittextFat.text.toString().toFloatOrNull() ?: 0f
            val total = max(carbohydrate + protein + fat, 1f)

            binding.layoutFoodDetail.progressBarCarbohydrate.setProgressWithAnimation((carbohydrate / total * 1000).toInt())
            binding.layoutFoodDetail.progressBarProtein.setProgressWithAnimation((protein / total * 1000).toInt())
            binding.layoutFoodDetail.progressBarFat.setProgressWithAnimation((fat / total * 1000).toInt())
        }

        binding.layoutFoodDetail.edittextProtein.addTextChangedListener {
            if (!isAnimated) return@addTextChangedListener

            val carbohydrate = binding.layoutFoodDetail.edittextCarbohydrate.text.toString().toFloatOrNull() ?: 0f
            val protein = binding.layoutFoodDetail.edittextProtein.text.toString().toFloatOrNull() ?: 0f
            val fat = binding.layoutFoodDetail.edittextFat.text.toString().toFloatOrNull() ?: 0f
            val total = max(carbohydrate + protein + fat, 1f)

            binding.layoutFoodDetail.progressBarCarbohydrate.setProgressWithAnimation((carbohydrate / total * 1000).toInt())
            binding.layoutFoodDetail.progressBarProtein.setProgressWithAnimation((protein / total * 1000).toInt())
            binding.layoutFoodDetail.progressBarFat.setProgressWithAnimation((fat / total * 1000).toInt())
        }

        binding.layoutFoodDetail.edittextFat.addTextChangedListener {
            if (!isAnimated) return@addTextChangedListener

            val carbohydrate = binding.layoutFoodDetail.edittextCarbohydrate.text.toString().toFloatOrNull() ?: 0f
            val protein = binding.layoutFoodDetail.edittextProtein.text.toString().toFloatOrNull() ?: 0f
            val fat = binding.layoutFoodDetail.edittextFat.text.toString().toFloatOrNull() ?: 0f
            val total = max(carbohydrate + protein + fat, 1f)

            binding.layoutFoodDetail.progressBarCarbohydrate.setProgressWithAnimation((carbohydrate / total * 1000).toInt())
            binding.layoutFoodDetail.progressBarProtein.setProgressWithAnimation((protein / total * 1000).toInt())
            binding.layoutFoodDetail.progressBarFat.setProgressWithAnimation((fat / total * 1000).toInt())
        }
    }


    private fun getPhoto() {
        cropImageLauncher.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1,1)
                setActivityMenuIconColor(Color.BLACK)
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


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}