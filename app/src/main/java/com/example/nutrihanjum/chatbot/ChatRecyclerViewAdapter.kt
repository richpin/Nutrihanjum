package com.example.nutrihanjum.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.nutrihanjum.chatbot.model.BotData
import com.example.nutrihanjum.chatbot.model.ChatBotResponseDTO
import com.example.nutrihanjum.chatbot.model.ChatData
import com.example.nutrihanjum.chatbot.model.UserData
import com.example.nutrihanjum.databinding.ItemChatBotBinding
import com.example.nutrihanjum.databinding.ItemChatUserBinding

class ChatRecyclerViewAdapter(private val chatList: ArrayList<ChatData>)
    : RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == ChatData.BOT) {
            BotViewHolder(ItemChatBotBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            )

        } else {
            UserViewHolder(ItemChatUserBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            )
        }
    }


    class BotViewHolder(private val binding: ItemChatBotBinding): ViewHolder(binding) {
        override fun bind(item: ChatData) {
            val data = item as BotData

            Glide.with(binding.root.context)
                .load(data.profileUrl)
                .circleCrop()
                .into(binding.imageviewBotProfile)

            binding.textviewBotName.text = data.name
            binding.textviewChat.text = data.message
        }
    }


    class UserViewHolder(private val binding: ItemChatUserBinding): ViewHolder(binding) {
        override fun bind(item: ChatData) {
            val data = item as UserData

            Glide.with(binding.root.context)
                .load(data.profileUrl)
                .circleCrop()
                .into(binding.imageviewUserProfile)

            binding.textviewUserName.text = data.name
            binding.textviewChat.text = data.message
        }
    }


    fun notifyDataAdded() {
        notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(itemCount - 1)
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return chatList[position].type
    }

    open class ViewHolder(binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        open fun bind(item: ChatData) {}
    }
}
