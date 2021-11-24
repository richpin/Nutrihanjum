package com.example.nutrihanjum.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.FoodDTO
import com.example.nutrihanjum.repository.DiaryRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class DiaryViewModel : ViewModel() {

    // for add diary activity
    private val _diary = MutableLiveData<ContentDTO>()
    val diary: LiveData<ContentDTO> = _diary

    private val _foodList = MutableLiveData<ArrayList<FoodDTO>>(arrayListOf())
    val foodList: LiveData<ArrayList<FoodDTO>> = _foodList

    var workingPosition = -1
    var workingItem: FoodDTO? = null


    private val _foodAutoComplete = MutableLiveData<ArrayList<FoodDTO>>(arrayListOf())
    val foodAutoComplete: LiveData<ArrayList<FoodDTO>> = _foodAutoComplete

    fun addDiary(content: ContentDTO, imageUri: String) = viewModelScope.launch {
        DiaryRepository.addDiary(content, imageUri).collect {
            _diary.postValue(it)
        }
    }


    fun modifyDiary(content: ContentDTO, imageUri: String?) {
        viewModelScope.launch {
            if (imageUri != null) {
                DiaryRepository.modifyDiaryWithPhoto(content, imageUri).collect {
                    _diary.postValue(it)
                }
            } else {
                DiaryRepository.modifyDiaryWithoutPhoto(content).collect {
                    _diary.postValue(it)
                }
            }
        }
    }


    fun loadFoodAutoComplete(foodName: String) {
        viewModelScope.launch {
            _foodAutoComplete.value?.clear()

            DiaryRepository.loadFoodList(foodName).collect {
                _foodAutoComplete.value?.addAll(it)
            }

            _foodAutoComplete.postValue(_foodAutoComplete.value)
        }
    }


    // for diary fragment
    private val _diaryList = MutableLiveData<ArrayList<ContentDTO>>(arrayListOf())
    val diaryList: LiveData<ArrayList<ContentDTO>> get() = _diaryList

    private val _diaryDeleteResult = MutableLiveData<Boolean>()
    val diaryDeleteResult: LiveData<Boolean> = _diaryDeleteResult

    fun deleteDiary(documentId: String, imageUrl: String) = viewModelScope.launch {
        DiaryRepository.deleteDiary(documentId, imageUrl).collect {
            _diaryDeleteResult.postValue(it)
        }
    }


    fun loadAllDiaryAtDate(date: String) = viewModelScope.launch {
        val list = _diaryList.value!!
        list.clear()

        DiaryRepository.loadAllDiaryAtDate(date).collect {
            list.addAll(it)
        }

        _diaryList.postValue(list)
    }
}