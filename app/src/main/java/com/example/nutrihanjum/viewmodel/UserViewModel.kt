package com.example.nutrihanjum.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.Repository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    val uid get() = Repository.uid
    val userEmail get() = Repository.userEmail
    val userID get() = Repository.userID
    val photoUrl get() = Repository.userPhoto

    private val _signed = MutableLiveData<Boolean>()
    val signed : LiveData<Boolean> get() = _signed

    private val _signOutResult = MutableLiveData<Boolean>()
    val signOutResult: LiveData<Boolean> get() = _signOutResult

    fun isSigned() = Repository.isSigned()

    fun signOut(context: Context) = viewModelScope.launch {
        var res = true

        Repository.signOut(context).collect {
            res = res && it
        }

        _signOutResult.postValue(res)
    }

    fun notifyUserSigned() {
        _signed.value = true
    }

    fun notifyUserSignedOut() {
        _signed.value = false
    }
}