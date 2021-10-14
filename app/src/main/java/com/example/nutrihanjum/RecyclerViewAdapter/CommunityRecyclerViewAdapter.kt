package com.example.nutrihanjum.RecyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO

class CommunityRecyclerViewAdapter()
    : RecyclerView.Adapter<CommunityRecyclerViewAdapter.ViewHolder>() {
    var contentDTOs = arrayListOf<ContentDTO>()

    fun updateList(data : ArrayList<ContentDTO>) {
        contentDTOs = data
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val communityitem_profile_imageview : ImageView
        val communityitem_profile_textview : TextView
        val communityitem_content_imageview : ImageView
        val communityitem_content_textview : TextView
        val press_like_imageview : ImageView
        val press_comment_imageview : ImageView
        val press_report_imageview : ImageView

        init{
            communityitem_profile_imageview = view.findViewById(R.id.communityitem_profile_imageview)
            communityitem_profile_textview = view.findViewById(R.id.communityitem_profile_textview)
            communityitem_content_imageview = view.findViewById(R.id.communityitem_content_imageview)
            communityitem_content_textview = view.findViewById(R.id.communityitem_content_textview)
            press_like_imageview = view.findViewById(R.id.press_like_imageview)
            press_comment_imageview = view.findViewById(R.id.press_comment_imageview)
            press_report_imageview = view.findViewById(R.id.press_report_imageview)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_community, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        Glide.with(viewHolder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHolder.communityitem_content_imageview)
        viewHolder.communityitem_content_textview.text = contentDTOs[position].content
    }

    override fun getItemCount() : Int {
        return contentDTOs.size
    }
}