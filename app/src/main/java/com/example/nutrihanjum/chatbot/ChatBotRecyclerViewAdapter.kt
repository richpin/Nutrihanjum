package com.example.nutrihanjum.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.chatbot.model.ChatBotDTO
import com.example.nutrihanjum.databinding.ItemChatBotProfileBinding

class ChatBotRecyclerViewAdapter(private var chatBotDTOs: ArrayList<ChatBotDTO>) :
    RecyclerView.Adapter<ChatBotRecyclerViewAdapter.ViewHolder>() {

    var chatBotListener: ((id: ChatBotDTO) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBotProfileBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        val viewHolder = ViewHolder(binding)

        binding.root.setOnClickListener {
            val pos = viewHolder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                chatBotListener?.let { it(chatBotDTOs[pos]) }
            }
        }

        return viewHolder
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind()
    }


    override fun getItemCount(): Int {
        return chatBotDTOs.size
    }


    fun updateDataSet(dataSet: ArrayList<ChatBotDTO>) {
        chatBotDTOs = dataSet
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemChatBotProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val pos = bindingAdapterPosition

            Glide.with(itemView.context)
                .load(chatBotDTOs[pos].profileUrl)
                .circleCrop()
                .into(binding.imageviewChatBotProfile)

            binding.textviewChatBotProfile.text = chatBotDTOs[pos].profileName
            binding.textviewChatBotCategory.text = chatBotDTOs[pos].category
            binding.textviewChatBotContent.text = chatBotDTOs[pos].content
        }
    }
}