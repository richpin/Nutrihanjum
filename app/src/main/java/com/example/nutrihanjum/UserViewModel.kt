package com.example.nutrihanjum

import android.content.Context
import android.net.Uri
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

    private val _signed = MutableLiveData<Boolean>()
    val signed : LiveData<Boolean> get() = _signed

    fun isSigned() = UserRepository.isSigned()

    fun signOut(context: Context) {
        UserRepository.signOut(context)
    }

    fun notifyUserSigned() {
        _signed.value = true
    }

    fun notifyUserSignedOut() {
        _signed.value = false
    }
}