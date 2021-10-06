package com.example.nutrihanjum.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutrihanjum.repository.Repository

class UserViewModel : ViewModel() {
    val userEmail get() = Repository.userEmail
    val userID get() = Repository.userID

    private val _signed = MutableLiveData<Boolean>()
    val signed : LiveData<Boolean> get() = _signed

    fun isSigned() = userID != null

    fun signOut() {
        if (isSigned()) {
            Repository.signOut()
        }
    }

    fun notifyUserSigned() {
        _signed.value = true
    }

    fun notifyUserSignedOut() {
        _signed.value = false
    }
}