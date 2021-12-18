package com.example.nutrihanjum.chatbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.R
import com.example.nutrihanjum.chatbot.model.ChatBotDTO
import com.example.nutrihanjum.databinding.ActivityChatBinding
import com.example.nutrihanjum.model.UserDTO

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
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    private fun initChatBot() {
        val chatBot = intent.getSerializableExtra("chatBot") as ChatBotDTO

        viewModel.initChatBot(chatBot)
        binding.layoutLoading.visibility = View.VISIBLE

        viewModel.initialized.observe(this) {
            binding.layoutLoading.visibility = View.GONE
        }
    }


    private fun initView() {
        binding.textviewChatBotName.text = viewModel.chatBot.profileName

        val adapter = ChatRecyclerViewAdapter(
            viewModel.chatList.value!!, viewModel.chatBot,
            UserDTO(
                name = viewModel.userName!!,
                profileUrl = if (viewModel.userPhoto == null) "" else viewModel.userPhoto.toString()
            )
        )

        binding.recyclerviewChat.adapter = adapter
        binding.recyclerviewChat.layoutManager = LinearLayoutManager(this)

        viewModel.chatList.observe(this) {
            if (it.isNotEmpty()) adapter.notifyDataAdded()
            binding.btnSendChat.isEnabled = true
        }

        adapter.quickReplyListener = { text, event -> viewModel.sendEvent(text, event) }

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



    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}