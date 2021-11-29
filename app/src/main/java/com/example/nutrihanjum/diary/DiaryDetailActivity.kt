package com.example.nutrihanjum.diary

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.databinding.ActivityDiaryDetailBinding
import com.example.nutrihanjum.databinding.LayoutPopupDeleteCheckBinding
import com.example.nutrihanjum.databinding.LayoutPopupDeleteModifyBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.util.ProgressBarAnimationUtil.setProgressWithNoAnimation
import kotlin.math.max

class DiaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryDetailBinding
    private lateinit var viewModel: DiaryViewModel

    private lateinit var diary: ContentDTO

    private lateinit var modifyDialog: Dialog
    private lateinit var modifyBinding: LayoutPopupDeleteModifyBinding
    private lateinit var deleteCheckDialog: Dialog
    private lateinit var deleteCheckBinding: LayoutPopupDeleteCheckBinding

    private lateinit var addDiaryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        diary = intent.getSerializableExtra("data") as ContentDTO

        initDialog()
        initView()

        addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.hasExtra("modifiedContent") == true) {
                diary = it.data?.getSerializableExtra("modifiedContent") as ContentDTO

                val mIntent = Intent()
                mIntent.putExtra("modifiedContent", diary)
                setResult(Activity.RESULT_OK, mIntent)
                initView()
            }
        }

        setContentView(binding.root)
    }


    private fun initDialog() {
        modifyDialog = Dialog(this)
        modifyBinding = LayoutPopupDeleteModifyBinding.inflate(layoutInflater)
        modifyDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        modifyDialog.setContentView(modifyBinding.root)

        deleteCheckDialog = Dialog(this)
        deleteCheckBinding = LayoutPopupDeleteCheckBinding.inflate(layoutInflater)
        deleteCheckDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteCheckDialog.setContentView(deleteCheckBinding.root)

        modifyBinding.btnPopupModify.setOnClickListener {
            val mIntent = Intent(this, AddDiaryActivity::class.java)
            mIntent.putExtra("content", diary)

            addDiaryLauncher.launch(mIntent)
            modifyDialog.dismiss()
        }

        modifyBinding.btnPopupDelete.setOnClickListener {
            deleteCheckDialog.show()
            modifyDialog.dismiss()
        }

        deleteCheckBinding.btnDeleteCheckYes.setOnClickListener {
            viewModel.deleteDiary(diary.id, diary.imageUrl)
        }

        deleteCheckBinding.btnDeleteCheckNo.setOnClickListener {
            deleteCheckDialog.dismiss()
        }
    }


    private fun initView() {
        initNutritionInfo()

        binding.layoutFoodDetail.edittextFoodName.isFocusable = false
        binding.layoutFoodDetail.edittextCalorie.isFocusable = false
        binding.layoutFoodDetail.edittextCarbohydrate.isFocusable = false
        binding.layoutFoodDetail.edittextProtein.isFocusable = false
        binding.layoutFoodDetail.edittextFat.isFocusable = false

        binding.layoutFoodDetail.edittextFoodName.setSelectAllOnFocus(false)
        binding.layoutFoodDetail.edittextCalorie.setSelectAllOnFocus(false)
        binding.layoutFoodDetail.edittextCarbohydrate.setSelectAllOnFocus(false)
        binding.layoutFoodDetail.edittextProtein.setSelectAllOnFocus(false)
        binding.layoutFoodDetail.edittextFat.setSelectAllOnFocus(false)

        Glide.with(this)
            .load(diary.imageUrl)
            .into(binding.imageviewPreview)

        binding.textviewMealTime.text = diary.mealTime
        binding.textviewMemo.text = diary.content

        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnMore.setOnClickListener {
            modifyDialog.show()
        }

        viewModel.diaryDeleteResult.observe(this) {
            if (it) {
                val mIntent = Intent()
                mIntent.putExtra("deletedContent", true)
                setResult(Activity.RESULT_OK, mIntent)
                finish()
            }
            else {
                Toast.makeText(this, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun initNutritionInfo() {
        val carbohydrate = diary.nutritionInfo.carbohydrate
        val protein = diary.nutritionInfo.protein
        val fat = diary.nutritionInfo.fat
        val total = max(carbohydrate + protein + fat, 1f)

        binding.layoutFoodDetail.edittextCalorie.setText(diary.nutritionInfo.calorie.toString())
        binding.layoutFoodDetail.edittextCarbohydrate.setText(carbohydrate.toString())
        binding.layoutFoodDetail.edittextProtein.setText(protein.toString())
        binding.layoutFoodDetail.edittextFat.setText(fat.toString())

        binding.layoutFoodDetail.progressBarCarbohydrate.setProgressWithNoAnimation((carbohydrate / total * 1000).toInt())
        binding.layoutFoodDetail.progressBarProtein.setProgressWithNoAnimation((protein / total * 1000).toInt())
        binding.layoutFoodDetail.progressBarFat.setProgressWithNoAnimation((fat / total * 1000).toInt())

        var foodName = ""

        diary.foods.forEach {
            foodName += (it.name + ", ")
        }

        if (foodName.isNotEmpty()) foodName = foodName.dropLast(2)

        binding.layoutFoodDetail.edittextFoodName.setText(foodName)
    }
}