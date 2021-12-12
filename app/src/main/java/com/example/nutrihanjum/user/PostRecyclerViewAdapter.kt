package com.example.nutrihanjum.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.PostDTO
import com.example.nutrihanjum.util.NHUtil.df
import java.util.*
import kotlin.collections.ArrayList

class PostRecyclerViewAdapter: RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder>()  {
    var postDTOs = arrayListOf<PostDTO>()

    fun initPosts() {
        val size = postDTOs.size
        postDTOs.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun updatePosts(data: ArrayList<PostDTO>) {
        postDTOs = data
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postitem_layout: RelativeLayout
        val postitem_title_textview: TextView
        val postitem_timestamp_textview: TextView

        init{
            postitem_layout = view.findViewById(R.id.postitem_layout)
            postitem_title_textview = view.findViewById(R.id.postitem_title_textview)
            postitem_timestamp_textview = view.findViewById(R.id.postitem_timestamp_textview)

            postitem_layout.setOnClickListener {
                val intent = Intent(it.context, PostDetailActivity::class.java)
                intent.putExtra("postDTO", postDTOs[bindingAdapterPosition])
                it.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.postitem_title_textview.text = postDTOs[position].title
        holder.postitem_timestamp_textview.text = df.format(postDTOs[position].timeStamp)
    }

    override fun getItemCount(): Int {
        return postDTOs.size
    }
}