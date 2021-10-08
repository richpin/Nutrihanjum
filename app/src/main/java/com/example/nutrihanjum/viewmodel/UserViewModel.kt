package com.example.nutrihanjum.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutrihanjum.repository.Repository

class UserViewModel : ViewModel() {
    val userEmail get() = Repository.userEmail
    val userID get() = Repository.userID
    val photoUrl get() = Repository.userPhoto

    private val _signed = MutableLiveData<Boolean>()
    val signed : LiveData<Boolean> get() = _signed

    fun isSigned() = Repository.isSigned()

    fun signOut(context: Context) {
        Repository.signOut(context)
    }

    fun notifyUserSigned() {
        _signed.value = true
    }

    fun notifyUserSignedOut() {
        _signed.value = false
    }
}