package com.example.nutrihanjum.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.RecyclerViewAdapter.ChatbotRecyclerViewAdapter
import com.example.nutrihanjum.databinding.ChatbotFragmentBinding
import com.example.nutrihanjum.model.ChatbotDTO
import com.example.nutrihanjum.viewmodel.ChatbotViewModel

class ChatbotFragment : Fragment() {
    private var _binding: ChatbotFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        @Volatile
        private var instance: ChatbotFragment? = null

        @JvmStatic
        fun getInstance(): ChatbotFragment = instance ?: synchronized(this) {
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
        _binding = ChatbotFragmentBinding.inflate(inflater, container, false)

        val temp: ArrayList<ChatbotDTO> = arrayListOf()
        temp.add(
            ChatbotDTO(
                "한줌버섯",
                "https://png.vector.me/files/images/8/3/831744/super_mario_mushroom.jpg",
                "아이영양",
                "아이가 쑥쑥 클 수 있게!!"
            )
        )
        temp.add(
            ChatbotDTO(
                "한줌근육",
                "https://www.urbanbrush.net/web/wp-content/uploads/edd/2019/07/urbanbrush-20190730024701335085.png",
                "근육건강",
                "근육질의 몸을 위해서라면!!"
            )
        )

        binding.chatbotfragmentRecylerview.adapter = ChatbotRecyclerViewAdapter(temp)
        binding.chatbotfragmentRecylerview.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatbotViewModel::class.java)
        // TODO: Use the ViewModel
    }
}