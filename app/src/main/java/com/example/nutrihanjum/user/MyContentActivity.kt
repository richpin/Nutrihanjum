package com.example.nutrihanjum.user

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.R
import com.example.nutrihanjum.community.CommunityRecyclerViewAdapter
import com.example.nutrihanjum.community.CommunityViewModel
import com.example.nutrihanjum.community.ContentViewModel
import com.example.nutrihanjum.databinding.ActivityMyContentBinding
import com.example.nutrihanjum.diary.DiaryViewModel
import com.example.nutrihanjum.model.ContentDTO

class MyContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyContentBinding

    private lateinit var contentViewModel: ContentViewModel
    private lateinit var communityViewModel: CommunityViewModel
    private lateinit var diaryViewModel: DiaryViewModel

    private val recyclerViewAdapter = CommunityRecyclerViewAdapter()
    private val layoutManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyContentBinding.inflate(layoutInflater)
        contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
        communityViewModel= ViewModelProvider(this).get(CommunityViewModel::class.java)
        diaryViewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)

        makeLauncher(recyclerViewAdapter)
        recyclerViewAdapter.initDialog(this)
        recyclerViewAdapter.initContents()

        binding.MyPostActivityRecyclerview.layoutManager = layoutManager
        binding.MyPostActivityRecyclerview.adapter = recyclerViewAdapter

        addLiveDataObserver()
        addViewListener()
        modifyLayoutManager()

        val contentId = intent.getStringExtra("contentId")

        if(contentId == null) {
            contentViewModel.loadMyContents()
        } else {
            contentViewModel.loadSelectedContent(contentId)
        }

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
        contentViewModel.myContents.observe(this, {
            recyclerViewAdapter.updateContents(it)
            recyclerViewAdapter.notifyItemRangeInserted(0, it.size)
        })
        contentViewModel.selectedContent.observe(this, { content ->
            if(content == null)
                Toast.makeText(this, this.getString(R.string.selected_post_null), Toast.LENGTH_LONG).show()
            else {
                recyclerViewAdapter.updateContents(arrayListOf(content))
                recyclerViewAdapter.notifyItemInserted(0)
            }
        })
    }

    private fun addViewListener() {
        binding.myPostActivityBackButton.setOnClickListener { onBackPressed() }

        with(recyclerViewAdapter){
            likeClickEvent = { first, second -> communityViewModel.eventLikes(first, second) }
            savedClickEvent = { first, second -> communityViewModel.eventSaved(first, second) }
            deleteContentEvent = { first, second -> diaryViewModel.deleteDiary(first, second)}
        }
    }

    private fun modifyLayoutManager() {
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
    }
}