package com.example.nutrihanjum.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.community.ContentViewModel
import com.example.nutrihanjum.databinding.ActivityPostBinding

class PostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostBinding

    private lateinit var viewModel: ContentViewModel

    private val recyclerViewAdapter = PostRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(ContentViewModel::class.java)

        //isFaq is false, it is anmt
        val isFaq = intent.getBooleanExtra("isFaq", false)

        recyclerViewAdapter.initPosts()
        binding.PostActivityRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.PostActivityRecyclerview.adapter = recyclerViewAdapter

        setTitle(isFaq)
        addLiveDataObserver()
        addViewListener()

        viewModel.loadBannerImage(isFaq)
        viewModel.loadPosts(isFaq)

        setContentView(binding.root)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun addLiveDataObserver() {
        viewModel.posts.observe(this, Observer {
            recyclerViewAdapter.updatePosts(it)
            recyclerViewAdapter.notifyItemRangeInserted(0, it.size)
        })
        viewModel.bannerUri.observe(this, Observer {
            Glide.with(this).load(it).into(binding.postActivityBanner)
        })
    }

    private fun addViewListener() {
        binding.PostActivityBackButton.setOnClickListener { onBackPressed() }
    }

    private fun setTitle(isFaq: Boolean) {
        binding.postActivityTitleTextview.text = when(isFaq){
            true -> this.getString(R.string.faq)
            false -> this.getString(R.string.anmt)
        }
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}