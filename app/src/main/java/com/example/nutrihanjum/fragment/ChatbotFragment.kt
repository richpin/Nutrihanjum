package com.example.nutrihanjum.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nutrihanjum.R
import com.example.nutrihanjum.viewmodel.ChatbotViewModel

class ChatbotFragment private constructor() : Fragment() {

    companion object {
        @Volatile private var instance: ChatbotFragment? = null

        @JvmStatic fun getInstance(): ChatbotFragment = instance ?: synchronized(this) {
            instance ?: ChatbotFragment().also {
                instance = it
            }
        }
    }

    private lateinit var viewModel: ChatbotViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chatbot_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatbotViewModel::class.java)
        // TODO: Use the ViewModel
    }

}