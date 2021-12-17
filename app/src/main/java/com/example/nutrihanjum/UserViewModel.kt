package com.example.nutrihanjum

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.UserRepository
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

    private val _userDeleteResult = MutableLiveData<Boolean>()
    val userDeleteResult : LiveData<Boolean> get() = _userDeleteResult

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
}