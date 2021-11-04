package com.example.nutrihanjum.chatbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
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
        binding.layoutLoading.visibility = View.VISIBLE

        viewModel.initialized.observe(this) {
            binding.layoutLoading.visibility = View.GONE

            if (it) {
                viewModel.welcomeMessage()
            }
        }
    }


    private fun initView() {
        binding.textviewChatBotName.text = viewModel.chatBot.profileName

        val adapter = ChatRecyclerViewAdapter(viewModel.chatList.value!!)

        binding.recyclerviewChat.adapter = adapter
        binding.recyclerviewChat.layoutManager = LinearLayoutManager(this)

        viewModel.chatList.observe(this) {
            if (it.isNotEmpty()) adapter.notifyDataAdded()
            binding.btnSendChat.isEnabled = true
        }

        adapter.quickReplyListener = { viewModel.sendMessage(it) }

        addViewListener()
    }


    private fun addViewListener() {
        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.btnSendChat.setOnClickListener {
            val msg = binding.edittextChatInput.text.toString()

            if (msg.isEmpty()) {
                Toast.makeText(this@ChatActivity, getString(R.string.chat_empty_input), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.sendMessage(msg)
                binding.edittextChatInput.setText("")
                it.isEnabled = false
            }
        }

        binding.edittextChatInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                binding.btnSendChat.performClick()
            }

            return@setOnEditorActionListener true
        }
    }

}