package com.example.nutrihanjum.diary

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.ActivityDiaryDetailBinding
import com.example.nutrihanjum.databinding.LayoutPopupDeleteCheckBinding
import com.example.nutrihanjum.databinding.LayoutPopupDeleteModifyBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.util.ProgressBarAnimationUtil.setProgressWithNoAnimation
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlin.math.max

class DiaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryDetailBinding
    private lateinit var viewModel: DiaryViewModel

    private lateinit var modifyDialog: Dialog
    private lateinit var modifyBinding: LayoutPopupDeleteModifyBinding
    private lateinit var deleteCheckDialog: Dialog
    private lateinit var deleteCheckBinding: LayoutPopupDeleteCheckBinding

    private lateinit var addDiaryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        viewModel.content = intent.getSerializableExtra("content") as ContentDTO

        initView()

        if (savedInstanceState == null) {
            binding.btnMore.isClickable = false
            viewModel.loadDiaryById(viewModel.content.id)
            binding.layoutLoading.visibility = View.VISIBLE
        }

        addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.hasExtra("modifiedContent") == true) {
                viewModel.content = it.data?.getSerializableExtra("modifiedContent") as ContentDTO

                val mIntent = Intent()
                mIntent.putExtra("modifiedContent", viewModel.content)
                setResult(Activity.RESULT_OK, mIntent)
                setView()
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
            else if (viewModel.content != it) {
                viewModel.content = it
                setView()

                val mIntent = Intent()
                mIntent.putExtra("modifiedContent", viewModel.content)
                setResult(Activity.RESULT_OK, mIntent)
            }

            binding.btnMore.isClickable = true
        }

        setContentView(binding.root)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
            mIntent.putExtra("content", viewModel.content)

            addDiaryLauncher.launch(mIntent)
            modifyDialog.dismiss()
        }

        modifyBinding.btnPopupDelete.setOnClickListener {
            deleteCheckDialog.show()
            modifyDialog.dismiss()
        }

        deleteCheckBinding.btnDeleteCheckYes.setOnClickListener {
            viewModel.deleteDiary(viewModel.content.id, viewModel.content.imageUrl)
        }

        deleteCheckBinding.btnDeleteCheckNo.setOnClickListener {
            deleteCheckDialog.dismiss()
        }
    }


    private fun initView() {
        initHashTag()
        initDialog()

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

        binding.layoutFoodDetail.lineDivision.visibility = View.GONE

        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnMore.setOnClickListener {
            modifyDialog.show()
        }

        viewModel.diaryDeleteResult.observe(this) {
            if (it) {
                val mIntent = Intent()
                mIntent.putExtra("deletedContent", viewModel.content)
                setResult(Activity.RESULT_OK, mIntent)
                finish()
            }
            else {
                Toast.makeText(this, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        setView()
    }


    private fun setView() = synchronized(this) {
        Glide.with(this)
            .load(viewModel.content.imageUrl)
            .into(binding.imageviewPreview)

        binding.textviewMealTime.text = viewModel.content.mealTime
        binding.textviewMemo.text = viewModel.content.content

        setHashTag()
        setNutritionInfo()
    }


    private fun setNutritionInfo() {
        val carbohydrate = viewModel.content.nutritionInfo.carbohydrate
        val protein = viewModel.content.nutritionInfo.protein
        val fat = viewModel.content.nutritionInfo.fat
        val total = max(carbohydrate + protein + fat, 1f)

        binding.layoutFoodDetail.edittextCalorie.setText(viewModel.content.nutritionInfo.calorie.toString())
        binding.layoutFoodDetail.edittextCarbohydrate.setText(carbohydrate.toString())
        binding.layoutFoodDetail.edittextProtein.setText(protein.toString())
        binding.layoutFoodDetail.edittextFat.setText(fat.toString())

        binding.layoutFoodDetail.progressBarCarbohydrate.setProgressWithNoAnimation((carbohydrate / total * 1000).toInt())
        binding.layoutFoodDetail.progressBarProtein.setProgressWithNoAnimation((protein / total * 1000).toInt())
        binding.layoutFoodDetail.progressBarFat.setProgressWithNoAnimation((fat / total * 1000).toInt())

        var foodName = ""

        viewModel.content.foods.forEach {
            foodName += (it.name + ", ")
        }

        if (foodName.isNotEmpty()) foodName = foodName.dropLast(2)

        binding.layoutFoodDetail.edittextFoodName.setText(foodName)
    }


    private fun initHashTag() {
        binding.recyclerviewHashtag.adapter = HashTagAdapter()
        binding.recyclerviewHashtag.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        setHashTag()
    }


    private fun setHashTag() {
        (binding.recyclerviewHashtag.adapter as HashTagAdapter).updateHashTag(viewModel.content.hashTagList)
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}