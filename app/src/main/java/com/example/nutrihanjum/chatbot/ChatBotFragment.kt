package com.example.nutrihanjum.chatbot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.UserViewModel
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
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChatbotFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChatBotViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        initRecyclerView()

        viewModel.loadAllChatBots()

        return binding.root
    }


    private fun initRecyclerView() {
        val adapter = ChatBotRecyclerViewAdapter(arrayListOf())

        binding.recyclerviewChatBot.adapter = adapter
        binding.recyclerviewChatBot.setHasFixedSize(true)
        binding.recyclerviewChatBot.layoutManager = GridLayoutManager(activity, 2)

        viewModel.chatBotList.observe(viewLifecycleOwner) {
            adapter.updateDataSet(it)
        }

        adapter.chatBotListener = {
            if (userViewModel.isSigned()) {
                val mIntent = Intent(activity, ChatActivity::class.java)

                mIntent.putExtra("chatBot", it)

                startActivity(mIntent)
            }
            else {
                Toast.makeText(activity, "로그인 해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
