package com.example.nutrihanjum.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.CommunityRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val _savedContents = MutableLiveData<ArrayList<ContentDTO>>()
    val savedContents: LiveData<ArrayList<ContentDTO>> get() = _savedContents

    private val _myContents = MutableLiveData<ArrayList<ContentDTO>>()
    val myContents: LiveData<ArrayList<ContentDTO>> get() = _myContents

    fun loadSavedContents() = viewModelScope.launch {
        val list: ArrayList<ContentDTO> = arrayListOf()

        CommunityRepository.loadSavedContents().collect { item ->
            item?.let { list.add(item) }
        }
        _savedContents.postValue(list)
    }

    fun loadMyContents() = viewModelScope.launch {
        val list: ArrayList<ContentDTO> = arrayListOf()

        CommunityRepository.loadMyContents().collect { item ->
            item?.let { list.add(item) }
        }
        _myContents.postValue(list)
    }
}