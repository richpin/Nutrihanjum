package com.example.nutrihanjum.chatbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.R
import com.example.nutrihanjum.chatbot.model.ChatBotProfileDTO
import com.example.nutrihanjum.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatBotViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(ChatBotViewModel::class.java)

        initChatBot()
        initView()

        setContentView(binding.root)
    }


    private fun initChatBot() {
        val chatBot = intent.getSerializableExtra("chatBot") as ChatBotProfileDTO

        viewModel.initChatBot(chatBot)
    }


    private fun initView() {
        binding.textviewChatBotName.text = viewModel.chatBot.profileName

        val adapter = ChatRecyclerViewAdapter(viewModel.chatList.value!!)

        binding.recyclerviewChat.adapter = adapter
        binding.recyclerviewChat.layoutManager = LinearLayoutManager(this)

        viewModel.chatList.observe(this) {
            adapter.notifyDataAdded()
        }

        addViewListener()
    }


    private fun addViewListener() {

        viewModel.chatList.observe(this) {
            binding.btnBack.setOnClickListener { onBackPressed() }

            binding.btnSendChat.setOnClickListener {
                val msg = binding.edittextChatInput.text.toString()

                if (msg.isEmpty()) {
                    Toast.makeText(this@ChatActivity, getString(R.string.chat_empty_input), Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.sendMessage(msg)
                    it.isClickable = false
                }
            }
        }
    }

}