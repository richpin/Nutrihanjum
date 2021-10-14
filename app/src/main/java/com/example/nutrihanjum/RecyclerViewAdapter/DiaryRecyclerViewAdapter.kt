package com.example.nutrihanjum.RecyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.ItemDiaryBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.Repository
import com.example.nutrihanjum.viewmodel.DiaryViewModel
import java.util.ArrayList

class DiaryRecyclerViewAdapter(
    var diaryList: ArrayList<Pair<ContentDTO, String>>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onPopupClickListener: ((view: View, item: Pair<ContentDTO, String>) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding =
            ItemDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val viewHolder = ViewHolder(itemBinding)

        itemBinding.btnMore.setOnClickListener { v ->
            val pos = viewHolder.adapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                onPopupClickListener?.let { it(v, diaryList[pos]) }
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position)
    }

    override fun getItemCount() = diaryList.size

    inner class ViewHolder(val itemBinding: ItemDiaryBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(position: Int) {
            with(itemBinding) {
                Glide.with(communityitemContentImageview.context)
                    .load(diaryList[position].first.imageUrl)
                    .into(communityitemContentImageview)

                communityitemContentTextview.text = diaryList[position].first.content
                textviewMealTime.text = diaryList[position].first.mealTime
            }
        }
    }
}