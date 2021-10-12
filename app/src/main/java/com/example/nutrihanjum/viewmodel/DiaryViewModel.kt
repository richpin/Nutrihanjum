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
    private val _diarySetResult = MutableLiveData<Boolean>()
    val diarySetResult: LiveData<Boolean> get() = _diarySetResult

    private val _diaryList = MutableLiveData<ArrayList<Pair<ContentDTO, String>>>()
    val diaryList: LiveData<ArrayList<Pair<ContentDTO, String>>> get() = _diaryList

    fun setDiary(content: ContentDTO) = viewModelScope.launch {
        Repository.setDiary(content).collect {
            _diarySetResult.postValue(it)
        }
    }

    fun getDiary(date: String) = viewModelScope.launch {
        val list = ArrayList<Pair<ContentDTO,String>>()
        Repository.getDiary(date).collect {
            list!!.add(it)
        }

        _diaryList.postValue(list!!)
    }
}