package com.example.nutrihanjum.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.DiaryRepository
import com.example.nutrihanjum.repository.UserRepository
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.*

class CalendarDiaryViewModel: ViewModel() {

    // for diary fragment
    private val _diaryMap = MutableLiveData<MutableMap<Int,ArrayList<ContentDTO>>>(hashMapOf())
    val diaryMap: LiveData<MutableMap<Int, ArrayList<ContentDTO>>> get() = _diaryMap


    var updatePosition = RecyclerView.NO_POSITION

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

    fun loadDiaryInRange(start: LocalDate, end: LocalDate) = viewModelScope.launch {
        DiaryRepository.loadAllDiary(getFormattedDate(start), getFormattedDate(end)).collect {
            it.forEach { content ->
                addToMap(content)
            }
        }

        _diaryMap.postValue(_diaryMap.value)
    }


    fun refreshAll() {
        diaryMap.value?.clear()
        loadDiaryInRange(firstVisibleDate, lastVisibleDate)
    }



    fun addToMap(content: ContentDTO) {
        if (!_diaryMap.value!!.contains(content.date)) {
            _diaryMap.value!![content.date] = arrayListOf()
        }

        _diaryMap.value!![content.date]!!.add(content)
    }


    fun getDiaryList(_date: LocalDate): ArrayList<ContentDTO> {
        val date = getFormattedDate(_date)

        if (!_diaryMap.value!!.contains(date)) {
            _diaryMap.value!![date] = arrayListOf()
        }

        return _diaryMap.value!![date]!!
    }


    fun isDiaryEmptyAtDate(date: LocalDate) = diaryMap.value!![getFormattedDate(date)].isNullOrEmpty()

    fun getFormattedDate(date: LocalDate) : Int {
        return date.year * 10000 + date.monthValue * 100 + date.dayOfMonth
    }
}