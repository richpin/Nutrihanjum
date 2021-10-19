package com.example.nutrihanjum.community

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.databinding.CommunityFragmentBinding
import com.example.nutrihanjum.UserViewModel

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
        binding.communityfragmentRecylerview.adapter = recyclerViewAdapter

        communityViewModel.loadContents()
        communityViewModel.contents.observe(viewLifecycleOwner, Observer {
            recyclerViewAdapter.updateContents(it)
            recyclerViewAdapter.notifyDataSetChanged()
        })

        userViewModel.signed.observe(viewLifecycleOwner) {
            if(it) {
                recyclerViewAdapter.uid = userViewModel.uid!!
                recyclerViewAdapter.likeClickEvent = { first, second -> communityViewModel.eventLikes(first, second) }
                recyclerViewAdapter.savedClickEvent = { first, second -> communityViewModel.eventSaved(first, second)}
            } else {
                recyclerViewAdapter.uid = null
                recyclerViewAdapter.likeClickEvent = null
                recyclerViewAdapter.savedClickEvent = null
            }
        }

        return binding.root
    }
}