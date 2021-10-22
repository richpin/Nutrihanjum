package com.example.nutrihanjum.user

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.FragmentSignUpBinding
import kotlinx.coroutines.*

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SingUpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(SingUpViewModel::class.java)

        addViewListener()
        addLiveDataObserver()

        return binding.root
    }

    private val colorValid = Color.BLACK
    private val colorInvalid = Color.RED


    private fun addViewListener() {

        addTextInputValidChecker()

        binding.btnSignUp.setOnClickListener {
            if (viewModel.isValid) {
                val email = binding.edittextEmail.text.toString()
                val password = binding.edittextPassword.text.toString()
                val name = binding.edittextUserName.text.toString()

                viewModel.createUserWithEmail(email, password, name)
                binding.btnSignUp.isClickable = false
            } else {
                Toast.makeText(activity, getString(R.string.signup_not_filled), Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun addTextInputValidChecker() {
        binding.edittextUserName.addTextChangedListener(object: DelayedTextWatcher() {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkUserNameValid(editable.toString())
            }
        })

        binding.edittextEmail.addTextChangedListener(object: DelayedTextWatcher() {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkEmailValid(editable.toString())
            }
        })

        binding.edittextPassword.addTextChangedListener(object: DelayedTextWatcher() {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkPasswordValid(editable.toString())
            }
        })

        binding.edittextCheckPassword.addTextChangedListener(object: DelayedTextWatcher() {
            override fun delayedAfterChanged(editable: Editable?) {
                viewModel.checkPasswordSame(binding.edittextPassword.text.toString(), editable.toString())
            }
        })
    }


    private fun addLiveDataObserver() {

        viewModel.emailValid.observe(viewLifecycleOwner) {
            binding.edittextEmail.setTextColor(if (it) colorValid else colorInvalid)
        }

        viewModel.userNameValid.observe(viewLifecycleOwner) {
            binding.edittextUserName.setTextColor(if (it) colorValid else colorInvalid)
        }

        viewModel.passwordValid.observe(viewLifecycleOwner) {
            binding.edittextPassword.setTextColor(if (it) colorValid else colorInvalid)
            viewModel.checkPasswordSame(
                binding.edittextPassword.text.toString(),
                binding.edittextCheckPassword.text.toString()
            )
        }

        viewModel.passwordCheck.observe(viewLifecycleOwner) {
            binding.edittextCheckPassword.setTextColor(if (it) colorValid else colorInvalid)
        }

        viewModel.signUpResult.observe(viewLifecycleOwner) {
            if (it) {
                hideKeyboard()
                activity?.onBackPressed()
            } else {
                Toast.makeText(activity, getString(R.string.signup_failed), Toast.LENGTH_LONG).show()
            }

            binding.btnSignUp.isClickable = true
        }
    }


    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity?.currentFocus

        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    abstract inner class DelayedTextWatcher: TextWatcher {
        var job: Job? = null

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) = synchronized(this) {
            job?.cancel()

            job = lifecycleScope.launchWhenStarted {
                delay(300)
                delayedAfterChanged(p0)
            }
        }

        abstract fun delayedAfterChanged(editable: Editable?)
    }
}