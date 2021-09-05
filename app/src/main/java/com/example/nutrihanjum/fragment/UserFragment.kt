package com.example.nutrihanjum.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nutrihanjum.R
import com.example.nutrihanjum.viewmodel.UserViewModel

class UserFragment private constructor() : Fragment() {

    companion object {
        @Volatile private var instance: UserFragment? = null

        @JvmStatic fun getInstance(): UserFragment = instance ?: synchronized(this) {
            instance ?: UserFragment().also {
                instance = it
            }
        }
    }

    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        // TODO: Use the ViewModel
    }

}