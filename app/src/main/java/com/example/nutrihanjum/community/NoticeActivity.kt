package com.example.nutrihanjum.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.databinding.ActivityNoticeBinding
import com.example.nutrihanjum.repository.CommunityRepository.boardLimit

class NoticeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoticeBinding
    private var page = 1

    private lateinit var communityViewModel: CommunityViewModel

    private val recyclerViewAdapter = NoticeRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        communityViewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)

        binding.noticeActivityRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.noticeActivityRecyclerview.setHasFixedSize(true)

        addViewListener()
        addLiveDataObserver()

        recyclerViewAdapter.initNotices()
        binding.noticeActivityRecyclerview.adapter = recyclerViewAdapter

        communityViewModel.loadNoticesInit()

        setContentView(binding.root)
    }

    private fun addViewListener() {
        binding.noticeActivityBackButton.setOnClickListener { onBackPressed() }

        binding.noticeAcitvitySwiperefreshlayout.setOnRefreshListener {
            page = 1
            recyclerViewAdapter.initNotices()
            communityViewModel.loadNoticesInit()
            binding.noticeAcitvitySwiperefreshlayout.isRefreshing = false
        }

        binding.noticeActivityRecyclerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    page++
                    communityViewModel.loadNoticesMore()
                }
            }
        })
    }

    private fun addLiveDataObserver() {
        communityViewModel.notices.observe(this, {
            recyclerViewAdapter.updateNotices(it)

            val usersUid = arrayListOf<String>()
            it.forEach { notice ->
                if (recyclerViewAdapter.isUserEmpty(notice.senderId)) usersUid.add(notice.senderId)
            }

            communityViewModel.loadUserInfo(usersUid)
        })
        communityViewModel.users.observe(this, {
            it.forEach { user ->
                recyclerViewAdapter.users[user.first] = user.second
            }

            recyclerViewAdapter.notifyItemRangeInserted(
                ((page - 1) * boardLimit).toInt(),
                boardLimit.toInt()
            )
        })
    }
}