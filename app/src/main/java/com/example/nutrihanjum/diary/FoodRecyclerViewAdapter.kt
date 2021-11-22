package com.example.nutrihanjum.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.nutrihanjum.databinding.ItemFoodBinding
import com.example.nutrihanjum.databinding.ItemFoodEdittextBinding
import com.example.nutrihanjum.model.FoodDTO

class FoodRecyclerViewAdapter(private val foodList: ArrayList<FoodDTO>)
    : RecyclerView.Adapter<FoodRecyclerViewAdapter.ViewHolder>() {
    private val FOOD = 0
    private val TEXT = 1

    private var foodTextContent = ""

    var addFoodListener: ((food: FoodDTO, pos: Int) -> Unit)? = null
    var textChangeListener: ((name: String) -> Unit)? = null
    var revokeListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TEXT) {
            val itemBinding = ItemFoodEdittextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

            itemBinding.root.setOnClickListener {
                revokeListener?.invoke()
            }

            itemBinding.edittextFood.setOnEditorActionListener { textView, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    foodList.add(FoodDTO(textView.text.toString()))
                    textView.text = ""
                    foodTextContent = ""
                    notifyItemChanged(itemCount - 2)
                    notifyItemChanged(itemCount - 1)
                    addFoodListener?.invoke(foodList[itemCount-2], itemCount-2)
                    return@setOnEditorActionListener true
                }
                false
            }

            itemBinding.edittextFood.addTextChangedListener {
                foodTextContent = it.toString()
                textChangeListener?.invoke(it.toString())
            }

            TextViewHolder(itemBinding)
        } else {
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
                addFoodListener?.invoke(foodList[viewHolder.bindingAdapterPosition], viewHolder.bindingAdapterPosition)
            }

            viewHolder
        }
    }


    inner class TextViewHolder(private val binding: ItemFoodEdittextBinding): ViewHolder(binding) {
        override fun bind() {
            binding.edittextFood.setText(foodTextContent)
        }
    }

    inner class FoodViewHolder(private val binding: ItemFoodBinding): ViewHolder(binding) {
        override fun bind() {
            binding.textviewFoodName.text = foodList[bindingAdapterPosition].name
        }
    }

    override fun getItemCount() = foodList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) TEXT else FOOD
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    open inner class ViewHolder(binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        open fun bind() {}
    }
}