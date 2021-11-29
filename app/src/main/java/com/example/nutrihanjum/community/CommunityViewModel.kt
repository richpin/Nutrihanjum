package com.example.nutrihanjum.community

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.UserDTO
import com.example.nutrihanjum.repository.CommunityRepository
import com.example.nutrihanjum.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val _contents = MutableLiveData<ArrayList<ContentDTO>>()
    val contents: LiveData<ArrayList<ContentDTO>> get() = _contents

    private val _comments = MutableLiveData<ArrayList<ContentDTO.CommentDTO>>()
    val comments: LiveData<ArrayList<ContentDTO.CommentDTO>> get() = _comments

    private val _notices = MutableLiveData<ArrayList<UserDTO.NoticeDTO>>()
    val notices: LiveData<ArrayList<UserDTO.NoticeDTO>> get() = _notices

    private val _user = MutableLiveData<Pair<String,Pair<String, String>>>()
    val user: LiveData<Pair<String,Pair<String, String>>> get() = _user

    private val _bannerUri = MutableLiveData<Uri>()
    val bannerUri: LiveData<Uri> get() = _bannerUri

    fun loadContentsInit() = viewModelScope.launch {
        val list: ArrayList<ContentDTO> = arrayListOf()

        CommunityRepository.loadContentsInit().collect { item ->
            item?.let { list.add(item) }
        }
        _contents.postValue(list)
    }

    fun loadContentsMore() = viewModelScope.launch {
        contents.value?.let {
            val list: ArrayList<ContentDTO> = contents.value!!

            CommunityRepository.loadContentsMore().collect { item ->
                item?.let { list.add(item) }
            }
            _contents.postValue(list)
        }
    }

    fun loadNoticesInit() = viewModelScope.launch {
        val list: ArrayList<UserDTO.NoticeDTO> = arrayListOf()

        CommunityRepository.loadNoticesInit().collect { item ->
            item?.let { list.add(item) }
        }
        _notices.postValue(list)
    }

    fun loadNoticesMore() = viewModelScope.launch {
        val list: ArrayList<UserDTO.NoticeDTO> = notices.value!!

        CommunityRepository.loadNoticesMore().collect { item ->
            item?.let { list.add(item) }
        }
        _notices.postValue(list)
    }

    fun loadComments(contentId : String) = viewModelScope.launch {
        val list: ArrayList<ContentDTO.CommentDTO> = arrayListOf()

        CommunityRepository.loadComments(contentId).collect { item ->
            item?.let { list.add(item) }
        }
        _comments.postValue(list)
    }

    fun loadUserInfo(uid: String) = viewModelScope.launch {
        CommunityRepository.loadUserInfo(uid).collect { item ->
            item.let { _user.postValue(Pair(uid, item)) }
        }
    }

    fun loadBannerImage() = viewModelScope.launch {
        CommunityRepository.loadBannerImage().collect { item ->
            item.let { _bannerUri.postValue(item) }
        }
    }

    fun addComment(contentDTO: ContentDTO, commentDTO: ContentDTO.CommentDTO) = viewModelScope.launch {
        CommunityRepository.addComment(contentDTO, commentDTO).collect {
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