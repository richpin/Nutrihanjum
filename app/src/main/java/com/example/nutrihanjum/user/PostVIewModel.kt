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

    fun loadSavedContents() = viewModelScope.launch {
        val list: ArrayList<ContentDTO> = arrayListOf()

        CommunityRepository.loadSavedContents().collect { item ->
            list.add(item!!)
        }
        _savedContents.postValue(list)
    }
}