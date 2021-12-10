package com.example.nutrihanjum.diary

import android.util.Log
import androidx.lifecycle.*
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
    private val _diary = MutableLiveData<ContentDTO>()
    val diary: LiveData<ContentDTO> = _diary

    val foodList: ArrayList<FoodDTO> = arrayListOf()

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



    fun loadDiaryById(docId: String) = viewModelScope.launch {
        DiaryRepository.loadDiaryById(docId).collect {
            _diary.postValue(it)
        }
    }


    // for diary fragment
    private val _diaryMap = MutableLiveData<MutableMap<Int,ArrayList<ContentDTO>>>(hashMapOf())
    val diaryMap: LiveData<MutableMap<Int,ArrayList<ContentDTO>>> get() = _diaryMap

    private val _diaryDeleteResult = MutableLiveData<Boolean>()
    val diaryDeleteResult: LiveData<Boolean> = _diaryDeleteResult

    fun deleteDiary(documentId: String, imageUrl: String) = viewModelScope.launch {
        DiaryRepository.deleteDiary(documentId, imageUrl).collect {
            _diaryDeleteResult.postValue(it)
        }
    }


    var selectedDate: LocalDate = LocalDate.now()
        set(value) {
            currentDate = value
            field = value
        }
    var currentDate: LocalDate = LocalDate.now()
        set(value) {
            field = if (value.isBefore(signedDate)) signedDate
            else if (value.isAfter(today)) today
            else value
        }

    val lastVisibleDate: LocalDate = YearMonth.now().atEndOfMonth()
    var firstVisibleDate: LocalDate = YearMonth.now().minusMonths(10).atDay(1)
    val today: LocalDate = LocalDate.now()

    var firstMonth: YearMonth = YearMonth.now()
        set(value) {
            field = if (value.isBefore(signedDate.yearMonth)) {
                firstVisibleDate = signedDate.yearMonth.atDay(1)
                signedDate.yearMonth
            } else {
                firstVisibleDate = value.atDay(1)
                value
            }
        }

    var lastMonth: YearMonth = YearMonth.now()
    val signedDate: LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(UserRepository.signedDate!!), ZoneId.systemDefault()).toLocalDate()

    init {
        firstMonth = YearMonth.now().minusMonths(10)
    }

    var isWeekMode = true

    fun loadAllDiaryAtDate(date: Int) = viewModelScope.launch {
        val diaryMap = _diaryMap.value!!
        diaryMap[date] = arrayListOf()

        DiaryRepository.loadAllDiaryAtDate(date).collect {
            diaryMap[date]!!.addAll(it)
        }

        _diaryMap.postValue(diaryMap)
    }


    fun loadAllDiary(start: Int, end: Int) = viewModelScope.launch {
        DiaryRepository.loadAllDiary(start, end).collect {
            it.forEach { content ->
                addToMap(content)
            }
        }

        _diaryMap.postValue(_diaryMap.value)
    }



    fun addToMap(content: ContentDTO) {
        if (!_diaryMap.value!!.contains(content.date)) {
            _diaryMap.value!![content.date] = arrayListOf()
        }

        _diaryMap.value!![content.date]!!.add(content)
    }


    fun getDiaryList(date: Int): ArrayList<ContentDTO> {
        if (!_diaryMap.value!!.contains(date)) {
            _diaryMap.value!![date] = arrayListOf()
        }

        return _diaryMap.value!![date]!!
    }
}