package com.example.nutrihanjum.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.chatbot.model.BotData
import com.example.nutrihanjum.chatbot.model.ChatBotDTO
import com.example.nutrihanjum.chatbot.model.ChatData
import com.example.nutrihanjum.chatbot.model.UserData
import com.example.nutrihanjum.databinding.ItemChatBotBinding
import com.example.nutrihanjum.databinding.ItemChatUserBinding
import com.example.nutrihanjum.databinding.ItemQuickReplyBinding
import com.example.nutrihanjum.model.UserDTO

class ChatRecyclerViewAdapter(
    private val chatList: ArrayList<ChatData>,
    private val chatBot: ChatBotDTO,
    private val user: UserDTO
) : RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    var quickReplyListener: ((String, String) -> Unit)? = null

    private lateinit var recyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == ChatData.BOT) {
            val binding = ItemChatBotBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

            binding.textviewBotName.text = chatBot.profileName

            Glide.with(binding.root.context)
                .load(chatBot.profileUrl)
                .circleCrop()
                .into(binding.imageviewBotProfile)

            BotViewHolder(binding)

        } else {
            val binding = ItemChatUserBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

            if (user.profileUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(user.profileUrl)
                    .circleCrop()
                    .into(binding.imageviewUserProfile)
            }

            binding.textviewUserName.text = user.name

            UserViewHolder(binding)
        }
    }


   inner class BotViewHolder(private val binding: ItemChatBotBinding): ViewHolder(binding) {
        override fun bind(item: ChatData) {
            val data = item as BotData
            val pos = bindingAdapterPosition

            if (data.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(data.imageUrl)
                    .into(binding.imageviewReplyImage)

                binding.layoutImageReply.visibility = View.VISIBLE
                binding.layoutTextReply.visibility = View.GONE
            }
            else {
                binding.layoutImageReply.visibility = View.GONE
                binding.layoutTextReply.visibility = View.VISIBLE
                binding.textviewChat.text = data.message
            }

            data.quickReplies.forEachIndexed { index, quickReply ->
                val quickReplyBinding = if (binding.layoutQuickReplies.childCount <= index) {
                    val replyBinding = ItemQuickReplyBinding.inflate(
                        LayoutInflater.from(binding.layoutQuickReplies.context),
                        binding.layoutQuickReplies,
                        false
                    )

                    binding.layoutQuickReplies.addView(replyBinding.root)

                    replyBinding
                }
                else {
                    ItemQuickReplyBinding.bind(binding.layoutQuickReplies.getChildAt(index))
                }

                quickReplyBinding.btnAction.text = quickReply.text
                quickReplyBinding.root.visibility = View.VISIBLE

                if (pos == itemCount - 1) {
                    quickReplyBinding.btnAction.isEnabled = true
                    quickReplyBinding.btnAction.isChecked = false

                    quickReplyBinding.btnAction.setOnClickListener { btn ->
                        btn.isSelected = true
                        data.quickReplies[index].isSelected = true

                        binding.layoutQuickReplies.children.forEach { view ->
                            view.findViewById<View>(R.id.btn_action).isEnabled = false
                        }
                        quickReplyListener?.invoke(quickReply.text, quickReply.event)
                    }
                } else {
                    quickReplyBinding.btnAction.isEnabled = false
                    quickReplyBinding.btnAction.isChecked = quickReply.isSelected
                }
            }

            for (index in data.quickReplies.size until binding.layoutQuickReplies.childCount) {
                binding.layoutQuickReplies.getChildAt(index).visibility = View.GONE
            }
        }
    }


    inner class UserViewHolder(private val binding: ItemChatUserBinding): ViewHolder(binding) {
        override fun bind(item: ChatData) {
            val data = item as UserData

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

    open inner class ViewHolder(binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        open fun bind(item: ChatData) {}
    }
}
