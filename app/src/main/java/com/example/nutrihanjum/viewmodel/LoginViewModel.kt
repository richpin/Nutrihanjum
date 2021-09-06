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
    private val _loginResult = MutableLiveData<Pair<Boolean, String?>>()
    val loginResult : LiveData<Pair<Boolean, String?>> get() = _loginResult

    fun authWithGoogle(credential : AuthCredential) = viewModelScope.launch {
        Repository.authWithGoogle(credential).collect {
            _loginResult.postValue(it)
        }
    }
}