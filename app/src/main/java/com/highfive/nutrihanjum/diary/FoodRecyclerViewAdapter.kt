package com.example.nutrihanjum.diary


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.nutrihanjum.databinding.ItemFoodBinding
import com.example.nutrihanjum.model.FoodDTO

class FoodRecyclerViewAdapter(
    val foodList: ArrayList<FoodDTO>,
) : RecyclerView.Adapter<FoodRecyclerViewAdapter.ViewHolder>() {

    var foodSetListener: ((food: FoodDTO, pos: Int) -> Unit)? = null
    var revokeListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = FoodViewHolder(itemBinding)

        itemBinding.btnCancel.setOnClickListener {
            revokeListener?.invoke()
            foodList.removeAt(viewHolder.bindingAdapterPosition)
            notifyItemRemoved(viewHolder.bindingAdapterPosition)
        }

        itemBinding.textviewFoodName.setOnClickListener {
            revokeListener?.invoke()
            foodSetListener?.invoke(foodList[viewHolder.bindingAdapterPosition], viewHolder.bindingAdapterPosition)
        }

        return viewHolder
    }

    inner class FoodViewHolder(private val binding: ItemFoodBinding): ViewHolder(binding) {
        override fun bind() {
            binding.textviewFoodName.text = foodList[bindingAdapterPosition].name
        }
    }

    override fun getItemCount() = foodList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    open inner class ViewHolder(binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        open fun bind() {}
    }
}