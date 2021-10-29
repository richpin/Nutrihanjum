package com.example.nutrihanjum.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.CommunityRepository
import com.example.nutrihanjum.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val _contents = MutableLiveData<ArrayList<ContentDTO>>()
    val contents: LiveData<ArrayList<ContentDTO>> get() = _contents

    private val _comments = MutableLiveData<ArrayList<ContentDTO.CommentDTO>>()
    val comments: LiveData<ArrayList<ContentDTO.CommentDTO>> get() = _comments

    fun loadContentsInit() = viewModelScope.launch {
        val list: ArrayList<ContentDTO> = arrayListOf()

        CommunityRepository.loadContentsInit().collect { item ->
            list.add(item!!)
        }
        _contents.postValue(list)
        Log.wtf("gang", "start")
    }

    fun loadContentsMore() = viewModelScope.launch {
        val list: ArrayList<ContentDTO> = arrayListOf()

        CommunityRepository.loadContentsMore().collect { item ->
            list.add(item!!)
        }
        _contents.postValue(list)
        Log.wtf("gang", "end")
    }

    fun loadComments(contentId : String) = viewModelScope.launch {
        val list: ArrayList<ContentDTO.CommentDTO> = arrayListOf()

        CommunityRepository.loadComments(contentId).collect { item ->
            list.add(item!!)
        }
        _comments.postValue((list))
    }

    fun addComment(contentId: String, commentDTO: ContentDTO.CommentDTO) = viewModelScope.launch {
        CommunityRepository.addComment(contentId, commentDTO).collect {
            if(!it) Log.wtf("AddComment", it.toString())
        }
    }

    fun deleteComment(contentId: String, commentId: String) = viewModelScope.launch {
        CommunityRepository.deleteComment(contentId, commentId).collect {
            if(!it) Log.wtf("DeleteComment", it.toString())
        }
    }

    fun eventLikes(contentDTO: ContentDTO, isLiked: Boolean) = viewModelScope.launch {
        CommunityRepository.eventLikes(contentDTO, isLiked).collect {
            if(!it) Log.wtf("Likes", it.toString())
        }
    }

    fun eventSaved(contentDTO: ContentDTO, isSaved: Boolean) = viewModelScope.launch {
        CommunityRepository.eventSaved(contentDTO, isSaved).collect {
            if(!it) Log.wtf("Saved", it.toString())
        }
    }
}