package com.example.nutrihanjum.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.UserRepository.uid

class CommentRecyclerViewAdapter : RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder>() {
    var commentDTOs = arrayListOf<ContentDTO.CommentDTO>()
    var users = hashMapOf<String, Pair<String, String>>()

    lateinit var deleteCommentEvent: ((String) -> Unit)
    var countChange: Int = 0

    fun updateComments(data: ArrayList<ContentDTO.CommentDTO>) {
        commentDTOs = data
    }

    fun isUserEmpty(uid: String): Boolean {
        return !users.containsKey(uid)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val comment_profile_imageview: ImageView
        val comment_profile_textview: TextView
        val comment_timeago_textview: TextView
        val comment_content_textview: TextView
        val btn_swipe_imageview: ImageView
        val comment_swipe_view: RelativeLayout
        val btn_swipe_task: RelativeLayout

        init {
            comment_profile_imageview = view.findViewById(R.id.comment_profile_imageview)
            comment_profile_textview = view.findViewById(R.id.comment_profile_textview)
            comment_timeago_textview = view.findViewById(R.id.comment_timeago_textview)
            comment_content_textview = view.findViewById(R.id.comment_content_textview)
            btn_swipe_imageview = view.findViewById(R.id.btn_swipe_imageview)
            comment_swipe_view = view.findViewById(R.id.comment_swipe_view)
            btn_swipe_task = view.findViewById(R.id.btn_swipe_task)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentRecyclerViewAdapter.ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(users[commentDTOs[position].uid]!!.second).circleCrop()
            .into(holder.comment_profile_imageview)
        holder.comment_profile_textview.text = users[commentDTOs[position].uid]!!.first
        holder.comment_timeago_textview.text = CommunityRecyclerViewAdapter.formatTime(
            holder.itemView.context,
            commentDTOs[position].timeStamp
        )
        holder.comment_content_textview.text = commentDTOs[position].comment

        if(commentDTOs[position].uid == uid){
            holder.btn_swipe_imageview.setImageResource(R.drawable.ic_trash)
        } else {
            holder.btn_swipe_imageview.setImageResource(R.drawable.ic_report)
        }
    }

    override fun getItemCount(): Int {
        return commentDTOs.size
    }
}