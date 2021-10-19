package com.example.nutrihanjum.userPage

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    val userEmail get() = UserRepository.userEmail
    val userName get() = UserRepository.userName
    val photoUrl get() = UserRepository.userPhoto

    private val _signed = MutableLiveData<Boolean>()
    val signed : LiveData<Boolean> get() = _signed

    private val _userProfileChanged = MutableLiveData<Boolean>()
    val userProfileChanged: LiveData<Boolean> get() = _userProfileChanged

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

    fun updateUserName(name: String) {
        viewModelScope.launch {
            UserRepository.updateUserProfileName(name).collect {
                _userProfileChanged.postValue(it)
            }
        }
    }

    fun updateUserPhoto(photo: Uri) {
        viewModelScope.launch {
            UserRepository.updateUserProfilePhoto(photo).collect {
                _userProfileChanged.postValue(it)
            }
        }
    }
}