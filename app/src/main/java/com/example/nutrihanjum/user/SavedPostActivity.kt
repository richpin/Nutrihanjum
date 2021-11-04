package com.example.nutrihanjum.user

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.community.CommunityRecyclerViewAdapter
import com.example.nutrihanjum.community.CommunityViewModel
import com.example.nutrihanjum.community.PostViewModel
import com.example.nutrihanjum.databinding.ActivitySavedPostBinding
import com.example.nutrihanjum.model.ContentDTO

class SavedPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySavedPostBinding

    private lateinit var postViewModel: PostViewModel
    private lateinit var communityViewModel: CommunityViewModel

    private val recyclerViewAdapter = CommunityRecyclerViewAdapter()
    private val layoutManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedPostBinding.inflate(layoutInflater)
        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)
        communityViewModel= ViewModelProvider(this).get(CommunityViewModel::class.java)

        addLiveDataObserver()
        addViewListener()
        modifyLayoutManager()
        makeLauncher(recyclerViewAdapter)
        recyclerViewAdapter.initDialog(this)

        binding.savedPostActivityRecyclerview.layoutManager = layoutManager
        binding.savedPostActivityRecyclerview.setHasFixedSize(true)

        binding.savedPostActivityRecyclerview.adapter = recyclerViewAdapter

        postViewModel.loadSavedContents()

        setContentView(binding.root)
    }

    private fun makeLauncher(adapter: CommunityRecyclerViewAdapter) {
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

        adapter.addDiaryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    with(adapter.contentPosition) {
                        if (this != -1) {
                            adapter.contentDTOs[this] = result.data?.getSerializableExtra("modifiedContent") as ContentDTO
                            adapter.notifyItemChanged(this)
                        }
                    }
                    adapter.contentPosition = -1
                }
            }
    }


    private fun addLiveDataObserver() {
        postViewModel.savedContents.observe(this, {
            recyclerViewAdapter.updateContents(it)
            recyclerViewAdapter.notifyDataSetChanged()
        })
    }

    private fun addViewListener() {
        binding.savedPostActivityBackButton.setOnClickListener { onBackPressed() }

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