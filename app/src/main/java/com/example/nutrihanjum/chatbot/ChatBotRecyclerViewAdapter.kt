package com.example.nutrihanjum.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.chatbot.model.ChatBotProfileDTO
import com.example.nutrihanjum.databinding.ItemChatBotProfileBinding

class ChatBotRecyclerViewAdapter(private var chatBotProfileDTOS: ArrayList<ChatBotProfileDTO>) :
    RecyclerView.Adapter<ChatBotRecyclerViewAdapter.ViewHolder>() {

    var chatBotListener: ((id: ChatBotProfileDTO) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBotProfileBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        val viewHolder = ViewHolder(binding)

        binding.root.setOnClickListener {
            val pos = viewHolder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                chatBotListener?.let { it(chatBotProfileDTOS[pos]) }
            }
        }

        return viewHolder
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind()
    }


    override fun getItemCount(): Int {
        return chatBotProfileDTOS.size
    }


    fun updateDataSet(dataSet: ArrayList<ChatBotProfileDTO>) {
        chatBotProfileDTOS = dataSet
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemChatBotProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val pos = bindingAdapterPosition

            Glide.with(itemView.context)
                .load(chatBotProfileDTOS[pos].profileUrl)
                .circleCrop()
                .into(binding.imageviewChatBotProfile)

            binding.textviewChatBotProfile.text = chatBotProfileDTOS[pos].profileName
            binding.textviewChatBotCategory.text = chatBotProfileDTOS[pos].category
            binding.textviewChatBotContent.text = chatBotProfileDTOS[pos].content
        }
    }
}