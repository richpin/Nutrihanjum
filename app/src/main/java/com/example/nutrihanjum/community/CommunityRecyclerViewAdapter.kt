package com.example.nutrihanjum.community

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.LayoutPopupMyBinding
import com.example.nutrihanjum.databinding.LayoutPopupOtherBinding
import com.example.nutrihanjum.databinding.LayoutPopupDeleteBinding
import com.example.nutrihanjum.databinding.LayoutPopupDeleteBinding.*
import com.example.nutrihanjum.diary.AddDiaryActivity
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.UserRepository.uid

class CommunityRecyclerViewAdapter() :
    RecyclerView.Adapter<CommunityRecyclerViewAdapter.ViewHolder>() {
    var contentDTOs = arrayListOf<ContentDTO>()
    var contentPosition = -1

    private lateinit var popupMyDialog: Dialog
    private lateinit var popupOtherDialog: Dialog
    private lateinit var popupDeleteDialog: Dialog
    private lateinit var popupMyBinding: LayoutPopupMyBinding
    private lateinit var popupOtherBinding: LayoutPopupOtherBinding
    private lateinit var popupDeleteBinding: LayoutPopupDeleteBinding

    private object TIME_MAXIMUM {
        const val SEC = 60
        const val MIN = 60
        const val HOUR = 24
        const val DAY = 30
        const val MONTH = 12
    }

    companion object {
        fun formatTime(mContext: Context, regTime: Long): String {
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
    }

    var likeClickEvent: ((ContentDTO, Boolean) -> Unit)? = null
    var savedClickEvent: ((ContentDTO, Boolean) -> Unit)? = null
    var deleteClickEvent: ((String, String) -> Unit)? = null
    lateinit var commentLauncher: ActivityResultLauncher<Intent>
    lateinit var addDiaryLauncher: ActivityResultLauncher<Intent>

    fun updateContents(data: ArrayList<ContentDTO>) {
        contentDTOs.addAll(data)
    }

    fun initContents() {
        contentDTOs.clear()
    }

    private fun isLiked(likes: List<String>): Boolean {
        return likes.contains(uid)
    }

    private fun isSaved(saved: List<String>): Boolean {
        return saved.contains(uid)
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
        val press_save_layout: LinearLayout
        val press_save_imageview: ImageView
        val press_like_imageview: ImageView
        val press_etc_imageview: ImageView

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
            press_save_layout = view.findViewById(R.id.press_save_layout)
            press_save_imageview = view.findViewById(R.id.press_save_imageview)
            press_like_imageview = view.findViewById(R.id.press_like_imageview)
            press_etc_imageview = view.findViewById(R.id.press_etc_imageview)

            addListener()
        }

        private fun addListener() {
            press_etc_imageview.setOnClickListener {
                if (contentDTOs[bindingAdapterPosition].uid == uid)
                    popupMyDialog.show()
                else
                    popupOtherDialog.show()

                contentPosition = bindingAdapterPosition
            }

            press_like_layout.setOnClickListener {
                likeClickEvent?.let {
                    it(
                        contentDTOs[bindingAdapterPosition],
                        isLiked(contentDTOs[bindingAdapterPosition].likes)
                    )
                    if (isLiked(contentDTOs[bindingAdapterPosition].likes)) {
                        contentDTOs[bindingAdapterPosition].likes.remove(uid)
                        communityitem_lccount_textview.text =
                            formatCount(
                                itemView.context,
                                contentDTOs[bindingAdapterPosition].likes.size,
                                contentDTOs[bindingAdapterPosition].commentCount
                            )
                        press_like_imageview.setImageResource(R.drawable.ic_favorite_border)
                    } else {
                        contentDTOs[bindingAdapterPosition].likes.add(uid!!)
                        communityitem_lccount_textview.text =
                            formatCount(
                                itemView.context,
                                contentDTOs[bindingAdapterPosition].likes.size,
                                contentDTOs[bindingAdapterPosition].commentCount
                            )
                        press_like_imageview.setImageResource(R.drawable.ic_favorite)
                    }
                }
            }
            press_save_layout.setOnClickListener {
                savedClickEvent?.let {
                    it(
                        contentDTOs[bindingAdapterPosition],
                        isSaved(contentDTOs[bindingAdapterPosition].saved)
                    )
                    if (isSaved(contentDTOs[bindingAdapterPosition].saved)) {
                        contentDTOs[bindingAdapterPosition].saved.remove(uid)
                        press_save_imageview.setImageResource(R.drawable.ic_bookmark_border)
                    } else {
                        contentDTOs[bindingAdapterPosition].saved.add(uid!!)
                        press_save_imageview.setImageResource(R.drawable.ic_bookmark)
                    }
                }
            }
            press_comment_layout.setOnClickListener {
                val intent = Intent(it.context, CommentActivity::class.java)
                intent.putExtra("contentDTO", contentDTOs[bindingAdapterPosition])
                contentPosition = bindingAdapterPosition
                commentLauncher.launch(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(contentDTOs[position].profileUrl)
            .circleCrop().into(holder.communityitem_profile_imageview)
        holder.communityitem_profile_textview.text = contentDTOs[position].profileName
        Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
            .into(holder.communityitem_content_imageview)
        holder.communityitem_lccount_textview.text =
            formatCount(
                holder.itemView.context,
                contentDTOs[position].likes.size,
                contentDTOs[position].commentCount
            )
        holder.communityitem_timeago_textview.text =
            formatTime(holder.itemView.context, contentDTOs[position].timestamp)
        holder.communityitem_content_textview.text = contentDTOs[position].content

        with(holder.press_like_imageview) {
            if (isLiked(contentDTOs[position].likes)) {
                setImageResource(R.drawable.ic_favorite)
            } else {
                setImageResource(R.drawable.ic_favorite_border)
            }
        }
        with(holder.press_save_imageview) {
            if (isSaved(contentDTOs[position].saved)) {
                setImageResource(R.drawable.ic_bookmark)
            } else {
                setImageResource(R.drawable.ic_bookmark_border)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            for (payload in payloads) {
                if (payload is String) {
                    if (TextUtils.equals(payload, "comment")) {
                        holder.communityitem_lccount_textview.text =
                            formatCount(
                                holder.itemView.context,
                                contentDTOs[position].likes.size,
                                contentDTOs[position].commentCount
                            )
                    }
                }
            }
        }
    }

    fun initDialog(mContext: Context) {
        popupMyDialog = Dialog(mContext)
        popupOtherDialog = Dialog(mContext)
        popupDeleteDialog = Dialog(mContext)

        popupMyBinding = LayoutPopupMyBinding.inflate(LayoutInflater.from(mContext))
        popupOtherBinding = LayoutPopupOtherBinding.inflate(LayoutInflater.from(mContext))
        popupDeleteBinding = inflate(LayoutInflater.from(mContext))
        popupMyDialog.setContentView(popupMyBinding.root)
        popupOtherDialog.setContentView(popupOtherBinding.root)
        popupDeleteDialog.setContentView(popupDeleteBinding.root)

        popupMyDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupOtherDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupDeleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addPopupListener(mContext)
    }

    private fun addPopupListener(mContext: Context) {
        popupMyBinding.btnPopupModify.setOnClickListener {
            popupMyDialog.dismiss()
            val intent = Intent(mContext, AddDiaryActivity::class.java)
            intent.putExtra("content", contentDTOs[contentPosition])
            addDiaryLauncher.launch(intent)
        }

        popupMyBinding.btnPopupDelete.setOnClickListener {
            popupMyDialog.dismiss()
            popupDeleteDialog.show()
        }

        popupDeleteBinding.btnDeleteCheckNo.setOnClickListener {
            popupDeleteDialog.dismiss()
            contentPosition = -1
        }

        popupDeleteBinding.btnDeleteCheckYes.setOnClickListener {
            popupDeleteDialog.dismiss()
            if (contentPosition != -1) {
                deleteClickEvent?.let {
                    it(
                        contentDTOs[contentPosition].id,
                        contentDTOs[contentPosition].imageUrl
                    )
                }
                contentDTOs.removeAt(contentPosition)
                notifyItemRemoved(contentPosition)
            }
            contentPosition = -1
        }
    }

    override fun getItemCount(): Int {
        return contentDTOs.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}