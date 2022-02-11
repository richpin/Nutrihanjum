package com.example.nutrihanjum.diary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.databinding.ItemAutoCompleteBinding
import com.example.nutrihanjum.model.FoodDTO

class AutoCompleteAdapter (
    context: Context,
    val foodList: ArrayList<FoodDTO>
) : ArrayAdapter<FoodDTO>(context, 0, foodList) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemAutoCompleteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        } else {
            ItemAutoCompleteBinding.bind(convertView)
        }

        binding.textviewFoodName.text = foodList[position].name

        return binding.root
    }


    inner class NoFilter: Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val results = FilterResults()

            results.values = foodList
            results.count = foodList.size

            return results
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    override fun getCount() = foodList.size
    override fun getItem(position: Int) = foodList[position]
    override fun getFilter() = noFilter

    private val noFilter = NoFilter()
}