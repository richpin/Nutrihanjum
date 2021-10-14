package com.example.nutrihanjum.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.Repository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class DiaryViewModel : ViewModel() {
    private val _diaryChanged = MutableLiveData<Boolean>()
    val diaryChanged: LiveData<Boolean> get() = _diaryChanged

    private val _diaryList = MutableLiveData<ArrayList<Pair<ContentDTO, String>>>()
    val diaryList: LiveData<ArrayList<Pair<ContentDTO, String>>> get() = _diaryList

    fun addDiary(content: ContentDTO, imageUri: String) = viewModelScope.launch {
        Repository.addDiary(content, imageUri).collect {
            _diaryChanged.postValue(it)
        }
    }

    fun deleteDiary(documentId: String, imageUrl: String) = viewModelScope.launch {
        Repository.deleteDiary(documentId, imageUrl).collect {
            _diaryChanged.postValue(it)
        }
    }

    fun modifyDiary(content: ContentDTO, documentId: String, imageUri: String?) {
        viewModelScope.launch {
            if (imageUri != null) {
                Repository.modifyDiaryWithPhoto(content, documentId, imageUri).collect {
                    _diaryChanged.postValue(it)
                }
            } else {
                Repository.modifyDiaryWithoutPhoto(content, documentId).collect {
                    _diaryChanged.postValue(it)
                }
            }
        }
    }

    fun loadAllDiaryAtDate(date: String) = viewModelScope.launch {
        val list = ArrayList<Pair<ContentDTO,String>>()

        Repository.loadAllDiaryAtDate(date).collect {
            list!!.add(it)
        }

        _diaryList.postValue(list!!)
    }
}