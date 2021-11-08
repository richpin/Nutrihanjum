package com.example.nutrihanjum.chatbot

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.chatbot.model.BotData
import com.example.nutrihanjum.chatbot.model.ChatBotResponseDTO
import com.example.nutrihanjum.chatbot.model.ChatData
import com.example.nutrihanjum.chatbot.model.UserData
import com.example.nutrihanjum.databinding.ItemChatBotBinding
import com.example.nutrihanjum.databinding.ItemChatUserBinding
import com.example.nutrihanjum.databinding.ItemQuickReplyBinding

class ChatRecyclerViewAdapter(private val chatList: ArrayList<ChatData>)
    : RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    var quickReplyListener: ((String) -> Unit)? = null

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


   inner class BotViewHolder(private val binding: ItemChatBotBinding): ViewHolder(binding) {
        override fun bind(item: ChatData) {
            val data = item as BotData
            val pos = bindingAdapterPosition

            Glide.with(binding.root.context)
                .load(data.profileUrl)
                .circleCrop()
                .into(binding.imageviewBotProfile)

            binding.textviewBotName.text = data.name
            binding.textviewChat.text = data.message

            binding.layoutQuickReplies.removeAllViews()

            data.quickReplies.forEachIndexed { index, quickReply ->
                val quickReplyBinding = ItemQuickReplyBinding.inflate(
                    LayoutInflater.from(binding.layoutQuickReplies.context),
                    binding.layoutQuickReplies,
                    false
                )

                quickReplyBinding.btnAction.text = quickReply.text

                if (pos == itemCount - 1) {
                    quickReplyBinding.btnAction.setOnClickListener { btn ->
                        btn.isSelected = true
                        data.quickReplies[index].isSelected = true

                        binding.layoutQuickReplies.children.forEach { view ->
                            view.findViewById<View>(R.id.btn_action).isEnabled = false
                        }
                        quickReplyListener?.invoke(quickReply.action)
                    }
                } else {
                    quickReplyBinding.btnAction.isEnabled = false
                    quickReplyBinding.btnAction.isChecked = quickReply.isSelected
                }
                binding.layoutQuickReplies.addView(quickReplyBinding.root)
            }
        }
    }


    inner class UserViewHolder(private val binding: ItemChatUserBinding): ViewHolder(binding) {
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
        if (itemCount > 1) notifyItemChanged(itemCount - 2)
        notifyItemInserted(itemCount - 1)
        recyclerView.scrollToPosition(itemCount - 1)
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView

        if (recyclerView.itemAnimator is SimpleItemAnimator) {
            (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return chatList[position].type
    }


    override fun getItemCount(): Int {
        return chatList.size
    }

    open class ViewHolder(binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        open fun bind(item: ChatData) {}
    }
}
