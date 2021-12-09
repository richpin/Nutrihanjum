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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.*
import com.example.nutrihanjum.diary.AddDiaryActivity
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.CommunityRepository.sendReportMail
import com.example.nutrihanjum.repository.UserRepository.uid
import com.example.nutrihanjum.util.NHUtil

import com.google.firebase.functions.FirebaseFunctionsException

class CommunityRecyclerViewAdapter() :
    RecyclerView.Adapter<CommunityRecyclerViewAdapter.ViewHolder>() {

    var contentDTOs = arrayListOf<ContentDTO>()
    var contentPosition = -1

    private lateinit var popupDeleteModifyDialog: Dialog
    private lateinit var popupReportDialog: Dialog
    private lateinit var popupDeleteCheckDialog: Dialog
    private lateinit var popupReportCheckDialog: Dialog
    private lateinit var popupReportResultDialog: Dialog
    private lateinit var popupDeleteModifyBinding: LayoutPopupDeleteModifyBinding
    private lateinit var popupReportBinding: LayoutPopupReportBinding
    private lateinit var popupDeleteCheckBinding: LayoutPopupDeleteCheckBinding
    private lateinit var popupReportCheckBinding: LayoutPopupReportCheckBinding
    private lateinit var popupReportResultBinding: LayoutPopupReportResultBinding

    var likeClickEvent: ((ContentDTO, Boolean) -> Unit)? = null
    var savedClickEvent: ((ContentDTO, Boolean) -> Unit)? = null
    var deleteContentEvent: ((String, String) -> Unit)? = null
    lateinit var commentLauncher: ActivityResultLauncher<Intent>
    lateinit var addDiaryLauncher: ActivityResultLauncher<Intent>

    fun updateContents(data: ArrayList<ContentDTO>) {
        contentDTOs = data
    }

    fun initContents() {
        val size = contentDTOs.size
        contentDTOs.clear()
        notifyItemRangeRemoved(0, size)
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
        val communityitem_content_layout: LinearLayout
        val communityitem_content_viewmore: TextView
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
            communityitem_content_layout = view.findViewById(R.id.communityitem_content_layout)
            communityitem_content_viewmore = view.findViewById(R.id.communityitem_content_viewmore)
            press_like_layout = view.findViewById(R.id.press_like_layout)
            press_comment_layout = view.findViewById(R.id.press_comment_layout)
            press_save_layout = view.findViewById(R.id.press_save_layout)
            press_save_imageview = view.findViewById(R.id.press_save_imageview)
            press_like_imageview = view.findViewById(R.id.press_like_imageview)
            press_etc_imageview = view.findViewById(R.id.press_etc_imageview)

            communityitem_content_viewmore.setOnClickListener {
                communityitem_content_textview.maxLines = Int.MAX_VALUE
                communityitem_content_viewmore.visibility = View.GONE
            }

            addListener()
        }

        private fun addListener() {
            press_etc_imageview.setOnClickListener {
                if (contentDTOs[bindingAdapterPosition].uid == uid)
                    popupDeleteModifyDialog.show()
                else
                    popupReportDialog.show()

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
            NHUtil.formatTime(holder.itemView.context, contentDTOs[position].timestamp)
            if (TextUtils.isEmpty(contentDTOs[position].content)) {
                holder.communityitem_content_layout.visibility = View.GONE
            } else {
                holder.communityitem_content_textview.text = contentDTOs[position].content
                setViewMore(holder.communityitem_content_textview,
                holder.communityitem_content_viewmore)
            }

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

    private fun setViewMore(contentTextView: TextView, viewMoreTextView: TextView) {
        contentTextView.post {
            val lineCount = contentTextView.layout.lineCount
            if (lineCount > 0) {
                if (contentTextView.layout.getEllipsisCount(lineCount - 1) > 0) {
                    viewMoreTextView.visibility = View.VISIBLE
                }
            } else {
                viewMoreTextView.visibility = View.GONE
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
        popupDeleteModifyDialog = Dialog(mContext)
        popupReportDialog = Dialog(mContext)
        popupDeleteCheckDialog = Dialog(mContext)
        popupReportCheckDialog = Dialog(mContext)
        popupReportResultDialog = Dialog(mContext)

        popupDeleteModifyBinding =
            LayoutPopupDeleteModifyBinding.inflate(LayoutInflater.from(mContext))
        popupReportBinding = LayoutPopupReportBinding.inflate(LayoutInflater.from(mContext))
        popupDeleteCheckBinding =
            LayoutPopupDeleteCheckBinding.inflate(LayoutInflater.from(mContext))
        popupReportCheckBinding =
            LayoutPopupReportCheckBinding.inflate(LayoutInflater.from(mContext))
        popupReportResultBinding = LayoutPopupReportResultBinding.inflate(LayoutInflater.from(mContext))
        popupDeleteModifyDialog.setContentView(popupDeleteModifyBinding.root)
        popupReportDialog.setContentView(popupReportBinding.root)
        popupDeleteCheckDialog.setContentView(popupDeleteCheckBinding.root)
        popupReportCheckDialog.setContentView(popupReportCheckBinding.root)
        popupReportResultDialog.setContentView(popupReportResultBinding.root)

        popupDeleteModifyDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupReportDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupDeleteCheckDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupReportCheckDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupReportResultDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addPopupListener(mContext)
    }

    private fun addPopupListener(mContext: Context) {
        popupDeleteModifyBinding.btnPopupModify.setOnClickListener {
            popupDeleteModifyDialog.dismiss()
            val intent = Intent(mContext, AddDiaryActivity::class.java)
            intent.putExtra("content", contentDTOs[contentPosition])
            addDiaryLauncher.launch(intent)
        }

        popupDeleteModifyBinding.btnPopupDelete.setOnClickListener {
            popupDeleteModifyDialog.dismiss()
            popupDeleteCheckDialog.show()
        }

        popupReportBinding.btnPopupReport.setOnClickListener {
            popupReportDialog.dismiss()
            popupReportCheckDialog.show()
        }

        popupDeleteCheckBinding.btnDeleteCheckNo.setOnClickListener {
            popupDeleteCheckDialog.dismiss()
            contentPosition = -1
        }

        popupReportCheckBinding.btnReportCheckNo.setOnClickListener {
            popupReportCheckDialog.dismiss()
            contentPosition = -1
        }

        popupDeleteCheckBinding.btnDeleteCheckYes.setOnClickListener {
            popupDeleteCheckDialog.dismiss()
            if (contentPosition != -1) {
                deleteContentEvent?.let {
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

        popupReportCheckBinding.btnReportCheckYes.setOnClickListener {
            popupReportCheckDialog.dismiss()
            sendReportMail(0, contentDTOs[contentPosition].id).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        popupReportResultBinding.btnPopupResult.text = mContext.getString(R.string.report_result_fail)
                        popupReportResultDialog.show()
                    }
                } else {
                    popupReportResultBinding.btnPopupResult.text = mContext.getString(R.string.report_result_success)
                    popupReportResultDialog.show()
                }
            }
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