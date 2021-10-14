package com.example.nutrihanjum.RecyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ChatbotDTO

class ChatbotRecyclerViewAdapter(private val chatbotDTOs: ArrayList<ChatbotDTO>) :
    RecyclerView.Adapter<ChatbotRecyclerViewAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatbot_profile_imageview: ImageView
        val chatbot_profile_textview: TextView
        val chatbot_category_textview: TextView
        val chatbot_content_textview: TextView

        init {
            chatbot_profile_imageview = view.findViewById(R.id.chatbot_profile_imageview)
            chatbot_profile_textview = view.findViewById(R.id.chatbot_profile_textview)
            chatbot_category_textview = view.findViewById(R.id.chatbot_category_textview)
            chatbot_content_textview = view.findViewById(R.id.chatbot_content_textview)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_chatbot, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Glide.with(viewHolder.itemView.context).load(chatbotDTOs[position].profileUrl)
            .circleCrop().into(viewHolder.chatbot_profile_imageview)
        viewHolder.chatbot_profile_textview.text = chatbotDTOs[position].profileName
        viewHolder.chatbot_category_textview.text = chatbotDTOs[position].category
        viewHolder.chatbot_content_textview.text = chatbotDTOs[position].content
    }

    override fun getItemCount(): Int {
        return chatbotDTOs.size
    }
}