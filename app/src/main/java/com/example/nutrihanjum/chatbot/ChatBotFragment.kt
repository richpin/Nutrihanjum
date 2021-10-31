package com.example.nutrihanjum.chatbot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.databinding.ChatbotFragmentBinding


class ChatBotFragment : Fragment() {
    private var _binding: ChatbotFragmentBinding? = null
    private val binding get() = _binding!!

    private val TAG = this.javaClass.simpleName

    companion object {
        @Volatile
        private var instance: ChatBotFragment? = null

        @JvmStatic
        fun getInstance(): ChatBotFragment = instance ?: synchronized(this) {
            instance ?: ChatBotFragment().also {
                instance = it
            }
        }
    }

    private lateinit var viewModel: ChatBotViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChatbotFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChatBotViewModel::class.java)

        initRecyclerView()

        viewModel.loadAllChatBots()

        return binding.root
    }


    private fun initRecyclerView() {
        val adapter = ChatBotRecyclerViewAdapter(arrayListOf())

        binding.recyclerviewChatBot.adapter = adapter
        binding.recyclerviewChatBot.setHasFixedSize(true)
        binding.recyclerviewChatBot.layoutManager = LinearLayoutManager(activity)

        viewModel.chatBotList.observe(viewLifecycleOwner) {
            adapter.updateDataSet(it)
        }

        adapter.chatBotListener = {
            val mIntent = Intent(activity, ChatActivity::class.java)

            mIntent.putExtra("chatBot", it)

            startActivity(mIntent)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
