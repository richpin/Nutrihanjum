package com.example.nutrihanjum.RecyclerViewAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.Resource
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.google.api.Distribution

class CommunityRecyclerViewAdapter() :
    RecyclerView.Adapter<CommunityRecyclerViewAdapter.ViewHolder>() {
    var contentDTOs = arrayListOf<ContentDTO>()

    private object TIME_MAXIMUM {
        const val SEC = 60
        const val MIN = 60
        const val HOUR = 24
        const val DAY = 30
        const val MONTH = 12
    }

    lateinit var isLiked: ((List<String>) -> Boolean)
    lateinit var isSaved: ((List<String>) -> Boolean)
    lateinit var likeClickEvent: ((ContentDTO) -> Unit)
    lateinit var savedClickEvent: ((ContentDTO) -> Unit)

    fun updateList(data: ArrayList<ContentDTO>) {
        contentDTOs = data
    }

    private fun formatTime(mContext: Context, regTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val diffSEC = (currentTime - regTime) / 1000
        val diffMIN = diffSEC / TIME_MAXIMUM.SEC
        val diffHOUR = diffMIN / TIME_MAXIMUM.MIN
        val diffDAY = diffHOUR / TIME_MAXIMUM.HOUR
        val diffMONTH = diffDAY / TIME_MAXIMUM.DAY
        val diffYEAR = diffMONTH / TIME_MAXIMUM.MONTH

        val msg: String = when {
            diffSEC < TIME_MAXIMUM.SEC -> mContext.getString(R.string.time_just_now)
            diffMIN < TIME_MAXIMUM.MIN -> diffMIN.toString() + mContext.getString(R.string.time_minute_before)
            diffHOUR < TIME_MAXIMUM.HOUR -> diffHOUR.toString() + mContext.getString(R.string.time_hour_before)
            diffDAY < TIME_MAXIMUM.DAY -> diffDAY.toString() + mContext.getString(R.string.time_day_before)
            diffMONTH < TIME_MAXIMUM.MONTH -> diffMONTH.toString() + mContext.getString(R.string.time_month_before)
            else -> diffYEAR.toString() + mContext.getString(R.string.time_year_before)
        }

        return msg
    }

    private fun formatCount(mContext: Context, like: Int, comment: Int): String {
        val msg =
            mContext.getString(R.string.like) + " " + like.toString() + mContext.getString(R.string.count) + " " + mContext.getString(
                R.string.comment
            ) + " " + comment.toString() + mContext.getString(R.string.count)

        return msg
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val communityitem_profile_imageview: ImageView
        val communityitem_profile_textview: TextView
        val communityitem_content_imageview: ImageView
        val communityitem_content_textview: TextView
        val communityitem_lccount_textview: TextView
        val communityitem_timeago_textview: TextView
        val press_like_layout: LinearLayout
        val press_comment_layout: LinearLayout
        val press_report_layout: LinearLayout
        val press_like_imageview: ImageView
        val press_saved_imageview: ImageView

        init {
            communityitem_profile_imageview =
                view.findViewById(R.id.communityitem_profile_imageview)
            communityitem_profile_textview = view.findViewById(R.id.communityitem_profile_textview)
            communityitem_content_imageview =
                view.findViewById(R.id.communityitem_content_imageview)
            communityitem_content_textview = view.findViewById(R.id.communityitem_content_textview)
            communityitem_lccount_textview = view.findViewById(R.id.communityitem_lccount_textview)
            communityitem_timeago_textview = view.findViewById(R.id.communityitem_timeago_textview)
            press_like_layout = view.findViewById(R.id.press_like_layout)
            press_comment_layout = view.findViewById(R.id.press_comment_layout)
            press_report_layout = view.findViewById(R.id.press_report_layout)
            press_like_imageview = view.findViewById(R.id.press_like_imageview)
            press_saved_imageview = view.findViewById(R.id.press_saved_imageview)

            press_like_layout.setOnClickListener {
                likeClickEvent(contentDTOs[adapterPosition])
            }
            press_saved_imageview.setOnClickListener {
                savedClickEvent(contentDTOs[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_community, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Glide.with(viewHolder.itemView.context).load(contentDTOs[position].imageUrl)
            .into(viewHolder.communityitem_content_imageview)
        viewHolder.communityitem_lccount_textview.text =
            formatCount(viewHolder.itemView.context, contentDTOs[position].likes.size, 0)
        viewHolder.communityitem_timeago_textview.text =
            formatTime(viewHolder.itemView.context, contentDTOs[position].timestamp)
        viewHolder.communityitem_content_textview.text = contentDTOs[position].content

        with(viewHolder.press_like_imageview) {
            if (isLiked(contentDTOs[position].likes)) {
                setImageResource(R.drawable.ic_favorite)
            } else {
                setImageResource(R.drawable.ic_favorite_border)
            }
        }
        with(viewHolder.press_saved_imageview) {
            if(isSaved(contentDTOs[position].saved)){
                setImageResource(R.drawable.ic_bookmark)
            } else {
                setImageResource(R.drawable.ic_bookmark_border)
            }
        }
    }

    override fun getItemCount(): Int {
        return contentDTOs.size
    }
}