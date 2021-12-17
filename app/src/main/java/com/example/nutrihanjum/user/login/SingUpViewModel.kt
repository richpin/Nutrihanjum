package com.example.nutrihanjum.user.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.repository.UserRepository
import com.example.nutrihanjum.util.NHPatternUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SingUpViewModel: ViewModel() {

    private val _signUpResult = MutableLiveData<Boolean>()
    val signUpResult: LiveData<Boolean> get() = _signUpResult

    private var emailValidJob: Job? = null
    private val _emailValid = MutableLiveData(false)
    val emailValid: LiveData<Boolean> get() = _emailValid

    private val _userNameValid = MutableLiveData(false)
    val userNameValid: LiveData<Boolean> get() = _userNameValid

    private val _passwordValid = MutableLiveData(false)
    val passwordValid: LiveData<Boolean> get() = _passwordValid

    private val _passwordCheck = MutableLiveData(false)
    val passwordCheck: LiveData<Boolean> get() = _passwordCheck


    val isValid get() = emailValid.value == true
            && passwordValid.value == true
            && userNameValid.value == true
            && passwordCheck.value == true


    fun checkUserNameValid(name: String) {
        _userNameValid.value = NHPatternUtil.USER_NAME.matcher(name).matches()
    }

    private val emailLock = Any()

    fun checkEmailValid(email: String) = synchronized(emailLock) {
        emailValidJob?.cancel()
        emailValidJob = null

        if (!NHPatternUtil.EMAIL.matcher(email).matches()) {
            _emailValid.value = false
            return@synchronized
        }

        emailValidJob = viewModelScope.launch {
            UserRepository.checkEmailUnique(email).collectLatest {
                _emailValid.postValue(it)
            }
        }
    }


    fun checkPasswordValid(password: String) {
        _passwordValid.value =
            NHPatternUtil.PASSWORD.matcher(password).matches()
    }


    fun checkPasswordSame(password: String, check: String) {
        _passwordCheck.value = password == check && passwordValid.value == true
    }


    fun createUserWithEmail(email: String, password: String, name: String) = viewModelScope.launch {
        UserRepository.createUserWithEmail(email, password, name).collect {
            _signUpResult.postValue(it)
        }
    }
}