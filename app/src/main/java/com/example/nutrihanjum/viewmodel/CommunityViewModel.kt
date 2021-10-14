package com.example.nutrihanjum.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutrihanjum.model.ContentDTO

class CommunityViewModel : ViewModel() {
    private val _contents = MutableLiveData<ArrayList<ContentDTO>>()
    val contents : LiveData<ArrayList<ContentDTO>> get() = _contents

    

    private fun loadContents() {

    }
}