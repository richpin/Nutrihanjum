package com.example.nutrihanjum.userPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.UserRepository
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Pair<Boolean, String?>>()
    val loginResult: LiveData<Pair<Boolean, String?>> get() = _loginResult

    fun authWithCredential(credential: AuthCredential) = viewModelScope.launch {
        UserRepository.authWithCredential(credential).collect {
            _loginResult.postValue(it)
        }
    }
}