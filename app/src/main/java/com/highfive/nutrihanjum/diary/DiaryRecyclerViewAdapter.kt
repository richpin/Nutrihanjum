package com.example.nutrihanjum.diary

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.ItemDiaryBinding
import com.example.nutrihanjum.databinding.LayoutNutritionInfoBlackBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.util.ProgressBarAnimationUtil.setProgressWithAnimation
import com.example.nutrihanjum.util.ProgressBarAnimationUtil.setProgressWithNoAnimation
import java.util.ArrayList
import kotlin.math.max

class DiaryRecyclerViewAdapter(
    var diaryList: ArrayList<ContentDTO>
): RecyclerView.Adapter<DiaryRecyclerViewAdapter.ViewHolder>() {

    var onDiaryClickListener: ((item: ContentDTO, pos: Int) -> Unit)? = null

    fun updateDiary(data: ArrayList<ContentDTO>) {
        diaryList = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            val itemBinding = LayoutNutritionInfoBlackBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

            itemBinding.edittextFoodName.inputType = InputType.TYPE_NULL
            itemBinding.edittextCalorie.inputType = InputType.TYPE_NULL
            itemBinding.edittextCarbohydrate.inputType = InputType.TYPE_NULL
            itemBinding.edittextProtein.inputType = InputType.TYPE_NULL
            itemBinding.edittextFat.inputType = InputType.TYPE_NULL

            itemBinding.edittextFoodName.setSelectAllOnFocus(false)
            itemBinding.edittextCalorie.setSelectAllOnFocus(false)
            itemBinding.edittextCarbohydrate.setSelectAllOnFocus(false)
            itemBinding.edittextProtein.setSelectAllOnFocus(false)
            itemBinding.edittextFat.setSelectAllOnFocus(false)

            HeaderViewHolder(itemBinding)
        }
        else {
            val itemBinding = ItemDiaryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

            val viewHolder = DiaryViewHolder(itemBinding)

            itemBinding.root.setOnClickListener {
                val pos = viewHolder.bindingAdapterPosition - 1

                if (pos != RecyclerView.NO_POSITION) {
                    onDiaryClickListener?.let { it(diaryList[pos], pos) }
                }
            }

            viewHolder
        }
    }



    inner class DiaryViewHolder(private val binding: ItemDiaryBinding): ViewHolder(binding) {
        override fun bind() {
            val diary = diaryList[bindingAdapterPosition - 1]

            with(binding) {
                Glide.with(imageviewPostImage.context)
                    .load(diary.imageUrl)
                    .into(imageviewPostImage)
            }

            val carbohydrate = diary.nutritionInfo.carbohydrate
            val protein = diary.nutritionInfo.protein
            val fat = diary.nutritionInfo.fat
            val total = max(carbohydrate + protein + fat, 1f)

            binding.layoutNutritionInfo.edittextCalorie.text = diary.nutritionInfo.calorie.toString()
            binding.layoutNutritionInfo.edittextCarbohydrate.text = carbohydrate.toString()
            binding.layoutNutritionInfo.edittextProtein.text = protein.toString()
            binding.layoutNutritionInfo.edittextFat.text = fat.toString()

            binding.layoutNutritionInfo.progressBarCarbohydrate.setProgressWithNoAnimation((carbohydrate / total * 1000).toInt())
            binding.layoutNutritionInfo.progressBarProtein.setProgressWithNoAnimation((protein / total * 1000).toInt())
            binding.layoutNutritionInfo.progressBarFat.setProgressWithNoAnimation((fat / total * 1000).toInt())

            var foodName = ""

            diary.foods.forEach {
                foodName += (it.name + ", ")
            }

            if (foodName.isNotEmpty()) foodName = foodName.dropLast(2)

            binding.layoutNutritionInfo.edittextFoodName.text = foodName
            binding.layoutNutritionInfo.textviewMealTime.text = diary.mealTime
        }
    }

    inner class HeaderViewHolder(private val binding: LayoutNutritionInfoBlackBinding): ViewHolder(binding) {
        override fun bind() {
            var calorie = 0f
            var carbohydrate = 0f
            var protein = 0f
            var fat = 0f

            diaryList.forEach {
                calorie += it.nutritionInfo.calorie
                carbohydrate += it.nutritionInfo.carbohydrate
                protein += it.nutritionInfo.protein
                fat += it.nutritionInfo.fat
            }

            binding.edittextCalorie.setText(calorie.toString())
            binding.edittextCarbohydrate.setText(carbohydrate.toString())
            binding.edittextProtein.setText(protein.toString())
            binding.edittextFat.setText(fat.toString())
            
            val total = max(carbohydrate + protein + fat, 1f)
            binding.progressBarCarbohydrate.setProgressWithAnimation((carbohydrate / total * 1000).toInt())
            binding.progressBarProtein.setProgressWithAnimation((protein / total * 1000).toInt())
            binding.progressBarFat.setProgressWithAnimation((fat / total * 1000).toInt())
        }
    }

    open inner class ViewHolder(binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        open fun bind() {}
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = diaryList.size + 1

    override fun getItemViewType(position: Int) = if (position == 0) 0 else 1

}