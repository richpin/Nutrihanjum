package com.example.nutrihanjum.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.RecyclerViewAdapter.CommunityRecyclerViewAdapter
import com.example.nutrihanjum.viewmodel.CommunityViewModel
import com.example.nutrihanjum.databinding.CommunityFragmentBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.viewmodel.UserViewModel
import com.google.firebase.firestore.auth.User

class CommunityFragment : Fragment() {
    private var _binding: CommunityFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        @Volatile private var instance: CommunityFragment? = null

        @JvmStatic fun getInstance(): CommunityFragment = instance ?: synchronized(this) {
            instance ?: CommunityFragment().also {
                instance = it
            }
        }
    }

    private lateinit var communityViewModel: CommunityViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CommunityFragmentBinding.inflate(inflater, container, false)

        communityViewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.communityfragmentRecylerview.layoutManager = linearLayoutManager
        binding.communityfragmentRecylerview.setHasFixedSize(true)

        binding.communityfragmentSwiperefreshlayout.setOnRefreshListener {
            communityViewModel.loadContents()
            binding.communityfragmentSwiperefreshlayout.isRefreshing = false
        }

        val recyclerViewAdapter = CommunityRecyclerViewAdapter()
        recyclerViewAdapter.uid = userViewModel.uid!!
        recyclerViewAdapter.likeClickEvent = { first, second -> communityViewModel.eventLikes(first, second) }
        recyclerViewAdapter.savedClickEvent = { first, second -> communityViewModel.eventSaved(first, second)}
        binding.communityfragmentRecylerview.adapter = recyclerViewAdapter

        communityViewModel.loadContents()
        communityViewModel.contents.observe(viewLifecycleOwner, Observer {
            recyclerViewAdapter.updateContents(it)
            recyclerViewAdapter.notifyDataSetChanged()
        })

        return binding.root
    }
}