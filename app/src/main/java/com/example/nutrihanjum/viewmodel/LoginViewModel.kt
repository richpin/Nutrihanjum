package com.example.nutrihanjum.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.Repository
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    val loginResult : MutableLiveData<Pair<Boolean, String?>> = MutableLiveData()
    val isSigned get() = Repository.userID != null

    fun authWithGoogle(credential : AuthCredential) = viewModelScope.launch {
        Repository.authWithGoogle(credential).collect {
            loginResult.postValue(it)
        }
    }
}