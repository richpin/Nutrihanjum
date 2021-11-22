package com.example.nutrihanjum.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.databinding.ItemAutoCompleteBinding
import com.example.nutrihanjum.model.FoodDTO

class AutoCompleteAdapter(private val foodList: ArrayList<FoodDTO>)
    : RecyclerView.Adapter<AutoCompleteAdapter.ViewHolder>() {

    var itemSelectedListener: ((food: FoodDTO) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemAutoCompleteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = ViewHolder(itemBinding)

        itemBinding.root.setOnClickListener {
            itemSelectedListener?.invoke(foodList[viewHolder.bindingAdapterPosition])
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = foodList.size


    inner class ViewHolder(private val binding: ItemAutoCompleteBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.textviewFoodName.text = foodList[bindingAdapterPosition].name
        }
    }
}