package com.example.nutrihanjum.diary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.databinding.ItemHashtagBinding

class HashTagAdapter: RecyclerView.Adapter<HashTagAdapter.ViewHolder>() {
    private var hashTagList: ArrayList<String> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHashtagBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = hashTagList.size


    inner class ViewHolder(private val binding: ItemHashtagBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind() {
            binding.textviewHashtag.text = "#${hashTagList[bindingAdapterPosition]}"
        }
    }

    fun updateHashTag(hashTag: List<String>) {
        hashTagList = ArrayList(hashTag)
        notifyDataSetChanged()
    }
}