package com.example.nutrihanjum.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.NewsDTO
import com.example.nutrihanjum.repository.NewsRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _news = MutableLiveData<ArrayList<NewsDTO>>()
    val news: LiveData<ArrayList<NewsDTO>> get() = _news

    private val _headNews = MutableLiveData<NewsDTO>()
    val headNews: LiveData<NewsDTO> = _headNews

    fun loadNewsInit() = viewModelScope.launch {
        val list: ArrayList<NewsDTO> = arrayListOf()

        NewsRepository.loadNewsInit().collect { item ->
            item?.let { list.add(item) }
        }
        _news.postValue(list)
    }

    fun loadNewsMore() = viewModelScope.launch {
        news.value?.let {
            val list: ArrayList<NewsDTO> = news.value!!

            NewsRepository.loadNewsMore().collect { item ->
                item?.let { list.add(item) }
            }
            _news.postValue(list)
        }
    }

    fun loadHeadNews() = viewModelScope.launch {
        NewsRepository.loadHeadNews().collect { item ->
            item?.let { _headNews.postValue(item) }
        }
    }
}