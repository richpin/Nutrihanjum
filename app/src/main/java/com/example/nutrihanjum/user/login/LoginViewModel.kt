package com.example.nutrihanjum.user.login

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.UserRepository
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LoginViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    fun authWithCredential(credential: AuthCredential) = viewModelScope.launch {
        UserRepository.authWithCredential(credential).collect {
            _loginResult.postValue(it)
        }
    }

    fun signInWithEmail(email: String, password: String, context: Context) = viewModelScope.launch {
        UserRepository.signInWithEmail(email, password, context).collect {
            _loginResult.postValue(it)
        }
    }

    fun signInWithKakaotalk(mContext: Context) = viewModelScope.launch {
        UserRepository.signInWithKakaotalk(mContext).collect {
            _loginResult.postValue(it)
        }
    }

    fun signInWithNaver(accessToken: String, mContext: Context) = viewModelScope.launch {
        UserRepository.signInWithNaver(accessToken, mContext).collect {
            _loginResult.postValue(it)
        }
    }
}