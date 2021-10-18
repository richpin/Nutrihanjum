package com.example.nutrihanjum.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.RecyclerViewAdapter.CommunityRecyclerViewAdapter
import com.example.nutrihanjum.viewmodel.CommunityViewModel
import com.example.nutrihanjum.databinding.CommunityFragmentBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.viewmodel.UserViewModel
import com.google.firebase.firestore.auth.User

class CommunityFragment private constructor() : Fragment() {
    private var _binding: CommunityFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        @Volatile
        private var instance: CommunityFragment? = null

        @JvmStatic
        fun getInstance(): CommunityFragment = instance ?: synchronized(this) {
            instance ?: CommunityFragment().also {
                instance = it
            }
        }
    }

    private lateinit var communityViewModel: CommunityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CommunityFragmentBinding.inflate(inflater, container, false)

        communityViewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)

        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.communityfragmentRecylerview.layoutManager = linearLayoutManager

        binding.communityfragmentRecylerview.setHasFixedSize(true)

        val recyclerViewAdapter = CommunityRecyclerViewAdapter()
        recyclerViewAdapter.isLiked = { communityViewModel.isLiked(it) }
        recyclerViewAdapter.isSaved = { communityViewModel.isSaved(it) }
        recyclerViewAdapter.likeClickEvent = { communityViewModel.eventLikes(it) }
        recyclerViewAdapter.savedClickEvent = { communityViewModel.eventSaved(it)}
        binding.communityfragmentRecylerview.adapter = recyclerViewAdapter

        communityViewModel.eventContents()
        communityViewModel.contents.observe(viewLifecycleOwner, Observer {
        })

        return binding.root
    }
}