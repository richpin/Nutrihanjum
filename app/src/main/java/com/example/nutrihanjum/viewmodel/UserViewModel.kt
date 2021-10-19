package com.example.nutrihanjum.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.Repository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    val userEmail get() = Repository.userEmail
    val userName get() = Repository.userName
    val photoUrl get() = Repository.userPhoto

    private val _signed = MutableLiveData<Boolean>()
    val signed : LiveData<Boolean> get() = _signed

    private val _userProfileChanged = MutableLiveData<Boolean>()
    val userProfileChanged: LiveData<Boolean> get() = _userProfileChanged

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

    fun updateUserName(name: String) {
        viewModelScope.launch {
            Repository.updateUserProfileName(name).collect {
                _userProfileChanged.postValue(it)
            }
        }
    }

    fun updateUserPhoto(photo: Uri) {
        viewModelScope.launch {
            Repository.updateUserProfilePhoto(photo).collect {
                _userProfileChanged.postValue(it)
            }
        }
    }
}