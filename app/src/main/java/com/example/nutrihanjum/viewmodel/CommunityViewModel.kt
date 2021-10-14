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

    fun getContents(): LiveData<ArrayList<ContentDTO>> {
        return _contents
    }
}