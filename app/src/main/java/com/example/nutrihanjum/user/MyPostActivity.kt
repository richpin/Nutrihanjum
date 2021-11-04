package com.example.nutrihanjum.user

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.community.CommunityRecyclerViewAdapter
import com.example.nutrihanjum.community.CommunityViewModel
import com.example.nutrihanjum.community.PostViewModel
import com.example.nutrihanjum.databinding.ActivityMyPostBinding

class MyPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPostBinding

    private lateinit var postViewModel: PostViewModel
    private lateinit var communityViewModel: CommunityViewModel

    private val recyclerViewAdapter = CommunityRecyclerViewAdapter()
    private val layoutManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostBinding.inflate(layoutInflater)
        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)
        communityViewModel= ViewModelProvider(this).get(CommunityViewModel::class.java)

        binding.MyPostActivityRecyclerview.layoutManager = layoutManager
        binding.MyPostActivityRecyclerview.setHasFixedSize(true)

        binding.MyPostActivityRecyclerview.adapter = recyclerViewAdapter

        makeCommentLauncher(recyclerViewAdapter)
        addLiveDataObserver()
        addViewListener()
        modifyLayoutManager()

        postViewModel.loadMyContents()

        setContentView(binding.root)
    }

    private fun makeCommentLauncher(adapter: CommunityRecyclerViewAdapter) {
        adapter.commentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val countChange = result.data?.getIntExtra("countChange", 0)

                    with(adapter.contentPosition) {
                        if (this != -1) {
                            countChange?.let {
                                adapter.contentDTOs[this].commentCount += it
                                adapter.notifyItemChanged(this, "comment")
                            }
                        }
                    }
                    adapter.contentPosition = -1
                }
            }
    }

    private fun addLiveDataObserver() {
        postViewModel.myContents.observe(this, {
            recyclerViewAdapter.updateContents(it)
            recyclerViewAdapter.notifyDataSetChanged()
        })
    }

    private fun addViewListener() {
        binding.MyPostActivityBackButton.setOnClickListener { onBackPressed() }

        recyclerViewAdapter.likeClickEvent =
            { first, second -> communityViewModel.eventLikes(first, second) }
        recyclerViewAdapter.savedClickEvent =
            { first, second -> communityViewModel.eventSaved(first, second) }
    }

    private fun modifyLayoutManager() {
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
    }
}