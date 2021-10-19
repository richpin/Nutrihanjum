package com.example.nutrihanjum.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.Repository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val _contents = MutableLiveData<ArrayList<ContentDTO>>()
    val contents: LiveData<ArrayList<ContentDTO>> get() = _contents

    fun loadContents() = viewModelScope.launch {
        val list: ArrayList<ContentDTO> = arrayListOf()

        Repository.loadContents().collect { item ->
            list.add(item!!)
        }
        _contents.postValue(list)
    }

    fun eventLikes(contentDTO: ContentDTO, isLiked: Boolean) = viewModelScope.launch {
        Repository.eventLikes(contentDTO, isLiked).collect {
            if(!it) Log.wtf("Likes", it.toString())
        }
    }

    fun eventSaved(contentDTO: ContentDTO, isSaved: Boolean) = viewModelScope.launch {
        Repository.eventSaved(contentDTO, isSaved).collect {
            if(!it) Log.wtf("Saved", it.toString())
        }
    }
}