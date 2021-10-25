package com.example.nutrihanjum.user

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.*
import com.example.nutrihanjum.repository.UserRepository
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class UpdateProfileViewModel: ViewModel() {
    val userName get() = UserRepository.userName
    val userEmail get() = UserRepository.userEmail
    val userPhoto get() = UserRepository.userPhoto

    private val photoUpdated = MutableLiveData(false)
    private val nameUpdated = MutableLiveData(false)
    private val emailUpdated = MutableLiveData(false)

    private val _updated = MutableLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = _updated


    private var emailValidJob: Job? = null
    private val _emailValid = MutableLiveData(true)
    val emailValid: LiveData<Boolean> get() = _emailValid

    private var userNameValidJob: Job? = null
    private val _userNameValid = MutableLiveData(true)
    val userNameValid: LiveData<Boolean> get() = _userNameValid

    val updateValid = emailValid.combineWith(userNameValid) { p0, p1 -> p0 == true && p1 == true }


    private val userNamePattern = Pattern.compile("^[가-힣a-zA-Z0-9_]+$")
    private val userNameLock = Any()

    fun checkUserNameValid(name: String) = synchronized(userNameLock) {
        userNameValidJob?.cancel()

        if (!userNamePattern.matcher(name).matches()) {
            _userNameValid.value = false
            return
        }

        userNameValidJob = viewModelScope.launch {
            UserRepository.checkUserNameUnique(name).collectLatest {
                _userNameValid.postValue(it)
            }
        }
    }


    private val emailLock = Any()

    fun checkEmailValid(email: String) = synchronized(emailLock) {
        emailValidJob?.cancel()
        emailValidJob = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailValid.value = false
            return
        }

        emailValidJob = viewModelScope.launch {
            UserRepository.checkEmailUnique(email).collectLatest {
                _emailValid.postValue(it)
            }
        }
    }


    fun updateProfile(email: String, name: String, photo: Uri?) = viewModelScope.launch {
        UserRepository.updateUserProfile(email, name, photo).collect {
            _updated.postValue(it)
        }
    }


    private fun <T, K, R> LiveData<T>.combineWith(
        liveData: LiveData<K>,
        block: (T?, K?) -> R
    ): LiveData<R> {
        val result = MediatorLiveData<R>()
        result.addSource(this) {
            result.value = block(this.value, liveData.value)
        }
        result.addSource(liveData) {
            result.value = block(this.value, liveData.value)
        }
        return result
    }

    private fun <T, K, L, R> LiveData<T>.combineWith(
        liveData: LiveData<K>,
        liveData2: LiveData<L>,
        block: (T?, K?, L?) -> R
    ): LiveData<R> {
        val result = MediatorLiveData<R>()
        result.addSource(this) {
            result.value = block(this.value, liveData.value, liveData2.value)
        }
        result.addSource(liveData) {
            result.value = block(this.value, liveData.value, liveData2.value)
        }
        result.addSource(liveData2) {
            result.value = block(this.value, liveData.value, liveData2.value)
        }
        return result
    }
}