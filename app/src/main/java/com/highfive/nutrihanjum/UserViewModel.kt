package com.example.nutrihanjum

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.model.NutritionInfo
import com.example.nutrihanjum.model.UserDTO
import com.example.nutrihanjum.repository.DiaryRepository
import com.example.nutrihanjum.repository.UserRepository
import com.example.nutrihanjum.util.NHUtil
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAccessor
import java.util.*

class UserViewModel : ViewModel() {
    val uid get() = UserRepository.uid
    val userEmail get() = UserRepository.userEmail
    val userName get() = UserRepository.userName
    val photoUrl get() = UserRepository.userPhoto


    private val _noticeFlag = MutableLiveData<Boolean>()
    val noticeFlag : LiveData<Boolean> get() = _noticeFlag

    private val _userDeleteResult = MutableLiveData<NHUtil.WithdrawResult>()
    val userDeleteResult : LiveData<NHUtil.WithdrawResult> get() = _userDeleteResult

    fun isSigned() = UserRepository.isSigned()

    fun signOut(context: Context) {
        UserRepository.signOut(context)
    }

    fun getNoticeFlag() = viewModelScope.launch {
        UserRepository.getNoticeFlag().collect { flag ->
            _noticeFlag.postValue(flag)
        }
    }

    fun setNoticeFlag(flag: Boolean)  {
        _noticeFlag.value = flag
    }

    fun updateNoticeFlag(isChecked: Boolean) = viewModelScope.launch {
        UserRepository.updateNoticeFlag(isChecked).collect {
            _noticeFlag.postValue(it xor (_noticeFlag.value ?: true))
        }
    }

    fun removeUser() = viewModelScope.launch {
        UserRepository.removeUser().collect {
            _userDeleteResult.postValue(it)
        }
    }



    private val _nutritionInfo = MutableLiveData<NutritionInfo>()
    val nutritionInfo : LiveData<NutritionInfo> get() = _nutritionInfo

    fun getTodayNutrition() = viewModelScope.launch {
        DiaryRepository.loadAllDiaryAtDate(getFormattedDate(LocalDate.now())).collect {
            val info = NutritionInfo()

            it.forEach {
                info.calorie += it.nutritionInfo.calorie
                info.carbohydrate += it.nutritionInfo.carbohydrate
                info.protein += it.nutritionInfo.protein
                info.fat += it.nutritionInfo.fat
            }

            _nutritionInfo.postValue(info)
        }
    }


    private val _user: MutableLiveData<UserDTO> = MutableLiveData()
    val user: LiveData<UserDTO> = _user

    fun getUserProfile() = viewModelScope.launch {
        UserRepository.getUserInfo().collect {
            _user.postValue(it)
        }
    }


    private fun getFormattedDate(date: LocalDate) : Int {
        return date.year * 10000 + date.monthValue * 100 + date.dayOfMonth
    }
}