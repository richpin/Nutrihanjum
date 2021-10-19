package com.example.nutrihanjum.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
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

class CommunityFragment : Fragment() {
    private var _binding : CommunityFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        @Volatile private var instance: CommunityFragment? = null

        @JvmStatic fun getInstance(): CommunityFragment = instance ?: synchronized(this) {
            instance ?: CommunityFragment().also {
                instance = it
            }
        }
    }

    private lateinit var viewModel: CommunityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CommunityFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)

        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        binding.communityfragmentRecylerview.layoutManager = linearLayoutManager
        binding.communityfragmentRecylerview.setHasFixedSize(true)

        val recyclerViewAdapter = CommunityRecyclerViewAdapter()
        recyclerViewAdapter.is_liked = { viewModel.is_liked(it) }
        recyclerViewAdapter.likeClickEvent = { viewModel.eventLikes(it) }
        recyclerViewAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.communityfragmentRecylerview.adapter = recyclerViewAdapter

        viewModel.eventContents()
        viewModel.getContents().observe(viewLifecycleOwner, Observer {
            recyclerViewAdapter.updateList(it)
            recyclerViewAdapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)

    }
}