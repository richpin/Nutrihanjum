package com.example.nutrihanjum.community

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.databinding.CommunityFragmentBinding
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.repository.CommunityRepository.boardLimit

class CommunityFragment : Fragment() {
    private var _binding: CommunityFragmentBinding? = null
    private val binding get() = _binding!!
    private var page = 1

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
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CommunityFragmentBinding.inflate(inflater, container, false)

        communityViewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        binding.communityfragmentRecylerview.layoutManager = LinearLayoutManager(activity)
        binding.communityfragmentRecylerview.setHasFixedSize(true)

        val recyclerViewAdapter = CommunityRecyclerViewAdapter()
        recyclerViewAdapter.commentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val countChange = result.data?.getIntExtra("countChange", 0)

                    with(recyclerViewAdapter.contentPosition) {
                        if (this != -1) {
                            countChange?.let {
                                recyclerViewAdapter.contentDTOs[this].commentCount += it
                                recyclerViewAdapter.notifyItemChanged(this, "comment")
                            }
                        }
                    }
                    recyclerViewAdapter.contentPosition = -1
                }
            }

        binding.communityfragmentRecylerview.adapter = recyclerViewAdapter

        binding.communityfragmentRecylerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!binding.communityfragmentRecylerview.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    page++
                    communityViewModel.loadContentsMore()
                }
            }
        })

        communityViewModel.loadContentsInit()
        communityViewModel.contents.observe(viewLifecycleOwner, Observer
        {
            recyclerViewAdapter.updateContents(it)
            recyclerViewAdapter.notifyItemRangeInserted(
                ((page - 1) * boardLimit).toInt(),
                boardLimit.toInt()
            )
        })

        userViewModel.signed.observe(viewLifecycleOwner)
        {
            if (it) {
                recyclerViewAdapter.likeClickEvent =
                    { first, second -> communityViewModel.eventLikes(first, second) }
                recyclerViewAdapter.savedClickEvent =
                    { first, second -> communityViewModel.eventSaved(first, second) }
            } else {
                recyclerViewAdapter.likeClickEvent = null
                recyclerViewAdapter.savedClickEvent = null
            }
        }

        binding.communityfragmentSwiperefreshlayout.setOnRefreshListener {
            page = 1
            recyclerViewAdapter.initContents()
            communityViewModel.loadContentsInit()
            binding.communityfragmentSwiperefreshlayout.isRefreshing = false
        }

        return binding.root
    }
}