package com.example.nutrihanjum.community

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.LayoutPopupDeleteCheckBinding
import com.example.nutrihanjum.databinding.LayoutPopupDeleteModifyBinding
import com.example.nutrihanjum.databinding.LayoutPopupReportBinding
import com.example.nutrihanjum.databinding.LayoutPopupReportCheckBinding
import com.example.nutrihanjum.diary.AddDiaryActivity
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.CommunityRepository
import com.example.nutrihanjum.repository.UserRepository.uid
import com.example.nutrihanjum.util.NHUtil
import com.google.firebase.functions.FirebaseFunctionsException

class CommentRecyclerViewAdapter : RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder>() {
    var commentDTOs = arrayListOf<ContentDTO.CommentDTO>()
    var users = hashMapOf<String, Pair<String, String>>()
    lateinit var contentId: String
    var commentPosition = -1

    private lateinit var popupDeleteCheckDialog: Dialog
    private lateinit var popupReportCheckDialog: Dialog
    private lateinit var popupDeleteCheckBinding: LayoutPopupDeleteCheckBinding
    private lateinit var popupReportCheckBinding: LayoutPopupReportCheckBinding

    var deleteCommentEvent: ((String, String) -> Unit)? = null
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
        if(users.contains(commentDTOs[position].uid)){
            if(!isEmpty(users[commentDTOs[position].uid]!!.second))
                Glide.with(holder.itemView.context).load(users[commentDTOs[position].uid]!!.second).circleCrop()
                    .into(holder.comment_profile_imageview)
            holder.comment_profile_textview.text = users[commentDTOs[position].uid]!!.first
        }
        holder.comment_timeago_textview.text = NHUtil.formatTime(
            holder.itemView.context,
            commentDTOs[position].timeStamp
        )
        holder.comment_content_textview.text = commentDTOs[position].comment

        if(commentDTOs[position].uid == uid){
            holder.btn_swipe_imageview.setImageResource(R.drawable.ic_trash_white)
            holder.btn_swipe_task.setOnClickListener {
                commentPosition = position
                popupDeleteCheckDialog.show() }
        } else {
            holder.btn_swipe_imageview.setImageResource(R.drawable.ic_report_white)
            holder.btn_swipe_task.setOnClickListener {
                commentPosition = position
                popupReportCheckDialog.show() }
        }
    }

    fun initDialog(mContext: Context) {
        popupDeleteCheckDialog = Dialog(mContext)
        popupReportCheckDialog = Dialog(mContext)

        popupDeleteCheckBinding = LayoutPopupDeleteCheckBinding.inflate(LayoutInflater.from(mContext))
        popupReportCheckBinding = LayoutPopupReportCheckBinding.inflate(LayoutInflater.from(mContext))
        popupDeleteCheckDialog.setContentView(popupDeleteCheckBinding.root)
        popupReportCheckDialog.setContentView(popupReportCheckBinding.root)

        popupDeleteCheckDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupReportCheckDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addPopupListener(mContext)
    }

    private fun addPopupListener(mContext: Context) {
        popupDeleteCheckBinding.btnDeleteCheckNo.setOnClickListener {
            popupDeleteCheckDialog.dismiss()
            commentPosition = -1
        }

        popupReportCheckBinding.btnReportCheckNo.setOnClickListener {
            popupReportCheckDialog.dismiss()
            commentPosition = -1
        }

        popupDeleteCheckBinding.btnDeleteCheckYes.setOnClickListener {
            popupDeleteCheckDialog.dismiss()
            if (commentPosition != -1) {
                deleteCommentEvent?.let {
                    it(
                        contentId,
                        commentDTOs[commentPosition].id
                    )
                }
                commentDTOs.removeAt(commentPosition)
                notifyItemRemoved(commentPosition)
                countChange -= 1
            }
            commentPosition = -1
        }

        popupReportCheckBinding.btnReportCheckYes.setOnClickListener {
            popupReportCheckDialog.dismiss()
            CommunityRepository.sendReportMail(0, contentId, commentDTOs[commentPosition].id).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        val detail = e.details
                        Toast.makeText(mContext, mContext.getString(R.string.report_result_success) + '(' + detail.toString() + ')', Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.report_result_success), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return commentDTOs.size
    }
}