package com.example.nutrihanjum.news

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.nutrihanjum.databinding.NewsFragmentBinding
import com.example.nutrihanjum.model.NewsDTO
import com.example.nutrihanjum.repository.CommunityRepository
import com.example.nutrihanjum.repository.CommunityRepository.boardLimit

class NewsFragment : Fragment() {
    private var _binding: NewsFragmentBinding? = null
    private val binding get() = _binding!!
    private var page = 1


    private lateinit var viewModel: NewsViewModel

    private val recyclerViewAdapter = NewsRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NewsFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(NewsViewModel::class.java)

        recyclerViewAdapter.initNews()
        binding.newsfragmentRecylerview.layoutManager = LinearLayoutManager(activity)
        binding.newsfragmentRecylerview.setHasFixedSize(true)
        binding.newsfragmentRecylerview.adapter = recyclerViewAdapter

        addLiveDataObserver()
        addViewListener()

        viewModel.loadHeadNews()
        viewModel.loadNewsInit()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addLiveDataObserver()
    }

    private fun addLiveDataObserver() {
        viewModel.news.observe(viewLifecycleOwner, Observer {
            recyclerViewAdapter.updateNews(it)
            recyclerViewAdapter.notifyItemRangeInserted(
                ((page - 1) * boardLimit).toInt(),
                boardLimit.toInt()
            )
        })

        viewModel.headNews.observe(viewLifecycleOwner, Observer {
            setHeadNewsView(it)
            addHeadListener(it)
        })
    }

    private fun addViewListener() {
        binding.newsfragmentRecylerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    page++
                    viewModel.loadNewsMore()
                }
            }
        })
    }

    private fun addHeadListener(headNews: NewsDTO) {
        binding.newsfragmentHeadLayout.setOnClickListener {
            val intent = Intent(context, NewsDetailActivity::class.java)
            intent.putExtra("webUrl", headNews.sourceUrl)
            startActivity(intent)
        }
    }

    private fun setHeadNewsView(headNews: NewsDTO) {
        Glide.with(this).load(headNews.imageUrl).into(binding.newsfragmentHeadImageview)
        binding.newsfragmentHeadTitleTextview.text = headNews.title
        binding.newsfragmentHeadSourceTextview.text = headNews.source
        binding.newsfragmentHeadCurtain.alpha = 0.5f
    }
}