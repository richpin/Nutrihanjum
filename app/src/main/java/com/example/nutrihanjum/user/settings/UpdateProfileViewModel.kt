package com.example.nutrihanjum.user.settings

import android.net.Uri
import androidx.lifecycle.*
import com.example.nutrihanjum.model.UserDTO
import com.example.nutrihanjum.repository.UserRepository
import com.example.nutrihanjum.util.NHPatternUtil
import com.example.nutrihanjum.util.NHUtil.combineWith
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UpdateProfileViewModel: ViewModel() {
    val userName get() = UserRepository.userName
    var userPhoto get() = photoURI ?: UserRepository.userPhoto
        set(value) {
            photoURI = value
        }

    var userAge get() = user.value?.age ?: 0
        set(value) {
            _user.value?.age = value
        }

    var userGender get() = user.value?.gender ?: ""
        set(value) {
            _user.value?.gender = value
        }

    // re-authenticate

    private val _reAuthResult = MutableLiveData<Boolean>()
    val reAuthResult: LiveData<Boolean> = _reAuthResult

    fun reAuthenticate(password: String) = viewModelScope.launch {
        UserRepository.reAuthenticate(password).collect {
            _reAuthResult.postValue(it)
        }
    }


    // profile update

    private var photoURI: Uri? = null
    private val _user: MutableLiveData<UserDTO> = MutableLiveData()
    val user: LiveData<UserDTO> = _user


    fun getUserProfile() = viewModelScope.launch {
        UserRepository.getUserInfo().collect {
            _user.postValue(it)
        }
    }


    val isPassword get() = UserRepository.authProvider == "password"

    private val _updated = MutableLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = _updated

    private val _userNameValid = MutableLiveData(true)
    val userNameValid: LiveData<Boolean> get() = _userNameValid

    private val _passwordValid = MutableLiveData(false)
    val passwordValid: LiveData<Boolean> get() = _passwordValid

    private val _passwordCheck = MutableLiveData(false)
    val passwordCheck: LiveData<Boolean> get() = _passwordCheck


    val updateValid = userNameValid
    val passwordUpdateValid = passwordValid.combineWith(passwordCheck) {a: Boolean?, b: Boolean? ->
        a == true && b == true
    }

    private val _passwordUpdated = MutableLiveData<Boolean>()
    val passwordUpdated: LiveData<Boolean> get() = _passwordUpdated


    fun checkUserNameValid(name: String) {
        _userNameValid.value = NHPatternUtil.USER_NAME.matcher(name).matches()
    }

    fun checkPasswordValid(password: String) {
        _passwordValid.value =
            NHPatternUtil.PASSWORD.matcher(password).matches()
    }

    fun checkPasswordSame(password: String, check: String) {
        _passwordCheck.value = password == check && passwordValid.value == true
    }



    fun updateProfile(name: String) = viewModelScope.launch {
        UserRepository.updateUserProfile(name, photoURI, userAge, userGender).collect {
            _updated.postValue(it)
        }
    }

    fun updatePassword(password: String) = viewModelScope.launch {
        UserRepository.updatePassword(password).collect {
            _passwordUpdated.postValue(it)
        }
    }

}