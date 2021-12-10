package com.example.nutrihanjum.community

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.UserDTO
import com.example.nutrihanjum.user.MyPostActivity
import com.example.nutrihanjum.util.NHUtil

class NoticeRecyclerViewAdapter() : RecyclerView.Adapter<NoticeRecyclerViewAdapter.ViewHolder>() {
    var noticeDTOs = arrayListOf<UserDTO.NoticeDTO>()
    var users = hashMapOf<String, Pair<String, String>>()

    fun updateNotices(data: ArrayList<UserDTO.NoticeDTO>) {
        noticeDTOs = data
    }

    fun initNotices() {
        val size = noticeDTOs.size
        noticeDTOs.clear()
        notifyDataSetChanged()
    }

    fun isUserEmpty(uid: String): Boolean {
        return !users.containsKey(uid)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val notice_name_textview: TextView
        val notice_profile_imageview: ImageView
        val notice_content_textview: TextView
        val notice_timeago_textview: TextView
        val notice_comment_textview: TextView
        val notice_content_imageview: ImageView
        val notice_item_layout: RelativeLayout

        init {
            notice_name_textview = view.findViewById(R.id.notice_name_textview)
            notice_profile_imageview = view.findViewById(R.id.notice_profile_imageview)
            notice_content_textview = view.findViewById(R.id.notice_content_textview)
            notice_timeago_textview = view.findViewById(R.id.notice_timeago_textview)
            notice_comment_textview = view.findViewById(R.id.notice_comment_textview)
            notice_content_imageview = view.findViewById(R.id.notice_content_imageview)
            notice_item_layout = view.findViewById(R.id.notice_item_layout)

            notice_item_layout.setOnClickListener {
                val intent = Intent(view.context, MyPostActivity::class.java)
                intent.putExtra("contentId", noticeDTOs[bindingAdapterPosition].contentId)
                startActivity(view.context, intent, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notice, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(users[noticeDTOs[position].uid]!!.second)
            .circleCrop().into(holder.notice_profile_imageview)
        holder.notice_name_textview.text = users[noticeDTOs[position].uid]!!.first
        with(holder.notice_comment_textview) {
            if (noticeDTOs[position].content.isEmpty()) visibility = View.GONE
            else text = noticeDTOs[position].content
        }
        holder.notice_timeago_textview.text = NHUtil.formatTime(
            holder.itemView.context,
            noticeDTOs[position].timestamp
        )
        Glide.with(holder.itemView.context).load(noticeDTOs[position].contentUrl)
            .into(holder.notice_content_imageview)

        with(holder.notice_content_textview) {
            when (noticeDTOs[position].kind) {
                0 -> this.text = holder.itemView.context.getString(R.string.notice_favorite)
                1 -> this.text = holder.itemView.context.getString(R.string.notice_comment)
            }
        }
    }

    override fun getItemCount(): Int {
        return noticeDTOs.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}