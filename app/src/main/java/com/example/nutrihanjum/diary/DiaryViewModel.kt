package com.example.nutrihanjum.diary

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.FoodDTO
import com.example.nutrihanjum.repository.DiaryRepository
import com.example.nutrihanjum.repository.UserRepository
import com.example.nutrihanjum.util.NHPatternUtil
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.*
import kotlin.collections.ArrayList

class DiaryViewModel: ViewModel() {


    // for add diary activity
    private val _diaryResult = MutableLiveData<ContentDTO>()
    val diaryResult: LiveData<ContentDTO> = _diaryResult

    val foodList: ArrayList<FoodDTO> = arrayListOf()

    var workingPosition = -1
    var workingItem: FoodDTO? = null

    var photoUrl: String = ""
    var content = ContentDTO()

    private val _foodAutoComplete = MutableLiveData<ArrayList<FoodDTO>>(arrayListOf())
    val foodAutoComplete: LiveData<ArrayList<FoodDTO>> = _foodAutoComplete

    fun addDiary(content: ContentDTO) = viewModelScope.launch {
        DiaryRepository.addDiary(content, photoUrl).collect {
            _diaryResult.postValue(it)
        }
    }


    fun modifyDiary(content: ContentDTO) {
        viewModelScope.launch {
            if (photoUrl.isNotEmpty()) {
                DiaryRepository.modifyDiaryWithPhoto(content, photoUrl).collect {
                    _diaryResult.postValue(it)
                }
            } else {
                DiaryRepository.modifyDiaryWithoutPhoto(content).collect {
                    _diaryResult.postValue(it)
                }
            }
        }
    }



    private val _diaryDeleteResult = MutableLiveData<Boolean>()
    val diaryDeleteResult: LiveData<Boolean> = _diaryDeleteResult

    fun deleteDiary(documentId: String, imageUrl: String) = viewModelScope.launch {
        DiaryRepository.deleteDiary(documentId, imageUrl).collect {
            _diaryDeleteResult.postValue(it)
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


    private val _diary = MutableLiveData<ContentDTO>()
    val diary: LiveData<ContentDTO> = _diary

    fun loadDiaryById(docId: String) = viewModelScope.launch {
        DiaryRepository.loadDiaryById(docId).collect {
            _diary.postValue(it)
        }
    }
}