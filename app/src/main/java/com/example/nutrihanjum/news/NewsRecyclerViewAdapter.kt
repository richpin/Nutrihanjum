package com.example.nutrihanjum.news

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.NewsDTO

class NewsRecyclerViewAdapter: RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder>() {
    var newsDTOs = arrayListOf<NewsDTO>()

    fun updateNews(data: ArrayList<NewsDTO>) {
        newsDTOs = data
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val newsitem_imageview: ImageView
        val newsitem_title_textview: TextView
        val newitem_explain_textview: TextView
        val newsitem_website_textview: TextView
        val newsitem_layout: LinearLayout

        init{
            newsitem_imageview = view.findViewById(R.id.newsitem_imageview)
            newsitem_title_textview = view.findViewById(R.id.newsitem_title_textview)
            newsitem_website_textview = view.findViewById(R.id.newsitem_website_textview)
            newsitem_layout = view.findViewById(R.id.newsitem_layout)
            newitem_explain_textview = view.findViewById(R.id.newitem_explain_textview)

            newsitem_layout.setOnClickListener {
                val intent = Intent(it.context, NewsDetailActivity::class.java)
                intent.putExtra("webUrl", newsDTOs[bindingAdapterPosition].sourceUrl)
                it.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsRecyclerViewAdapter.ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(newsDTOs[position].imageUrl).into(holder.newsitem_imageview)
        holder.newsitem_title_textview.text = newsDTOs[position].title
        holder.newsitem_website_textview.text = newsDTOs[position].source
        holder.newitem_explain_textview.text = newsDTOs[position].explain
    }

    override fun getItemCount(): Int {
        return newsDTOs.size
    }
}