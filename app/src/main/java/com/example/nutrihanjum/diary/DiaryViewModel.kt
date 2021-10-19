package com.example.nutrihanjum.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.DiaryRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class DiaryViewModel : ViewModel() {
    private val _diaryChanged = MutableLiveData<Boolean>()
    val diaryChanged: LiveData<Boolean> get() = _diaryChanged

    private val _diaryList = MutableLiveData<ArrayList<ContentDTO>>(arrayListOf())
    val diaryList: LiveData<ArrayList<ContentDTO>> get() = _diaryList

    fun addDiary(content: ContentDTO, imageUri: String) = viewModelScope.launch {
        DiaryRepository.addDiary(content, imageUri).collect {
            _diaryChanged.postValue(it)
        }
    }

    fun deleteDiary(documentId: String, imageUrl: String) = viewModelScope.launch {
        DiaryRepository.deleteDiary(documentId, imageUrl).collect {
            _diaryChanged.postValue(it)
        }
    }

    fun modifyDiary(content: ContentDTO, imageUri: String?) {
        viewModelScope.launch {
            if (imageUri != null) {
                DiaryRepository.modifyDiaryWithPhoto(content, imageUri).collect {
                    _diaryChanged.postValue(it)
                }
            } else {
                DiaryRepository.modifyDiaryWithoutPhoto(content).collect {
                    _diaryChanged.postValue(it)
                }
            }
        }
    }

    fun loadAllDiaryAtDate(date: String) = viewModelScope.launch {
        val list = _diaryList.value!!
        list.clear()

        DiaryRepository.loadAllDiaryAtDate(date).collect {
            list.add(it)
        }

        _diaryList.postValue(list)
    }
}