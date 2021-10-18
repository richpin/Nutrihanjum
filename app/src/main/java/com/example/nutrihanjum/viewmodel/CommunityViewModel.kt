package com.example.nutrihanjum.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.Repository
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val list = arrayListOf<ContentDTO>()

    private val _contents = MutableLiveData<ArrayList<ContentDTO>>()
    val contents: LiveData<ArrayList<ContentDTO>> get() = _contents

    init {
        _contents.postValue(list)
    }

    fun eventContents() = viewModelScope.launch {
        Repository.eventContents().collect { item ->
            when (item.type) {
                DocumentChange.Type.REMOVED -> list.removeAt(item.oldIndex)
                DocumentChange.Type.ADDED -> list.add(item.document.toObject(ContentDTO::class.java))
                DocumentChange.Type.MODIFIED -> list.set(
                    item.newIndex,
                    item.document.toObject(ContentDTO::class.java)
                )
            }

            _contents.postValue(list)
        }
    }

    fun eventLikes(contentDTO: ContentDTO) = viewModelScope.launch {
        Repository.eventLikes(contentDTO).collect {
            if(!it) Log.wtf("Likes", it.toString())
        }
    }

    fun eventSaved(contentDTO: ContentDTO) = viewModelScope.launch {
        Repository.eventSaved(contentDTO).collect {
            if(!it) Log.wtf("Saved", it.toString())
        }
    }

    fun isLiked(likes: List<String>): Boolean {
        return Repository.isLiked(likes)
    }

    fun isSaved(saved: List<String>): Boolean {
        return Repository.isSaved(saved)
    }
}