package com.example.nutrihanjum.user.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider

class                                                              LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel
    private lateinit var googleLoginLauncher : ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)

        setLoginListener()

        return binding.root
    }


    private fun googleLogin() {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(requireActivity(), gso)
        googleLoginLauncher.launch(client.signInIntent)
    }

    private fun setLoginListener() {

        googleLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val credential = GoogleAuthProvider.getCredential(task.result.idToken, null)
                viewModel.authWithCredential(credential)
            }
            catch(e: Exception) {
                Log.wtf(activity?.localClassName, e.message)
            }
        }

        binding.btnGoogleLogin.setOnClickListener {
            googleLogin()
        }

        binding.btnEmailLogin.setOnClickListener {
            val email = binding.edittextEmail.text.toString()
            val password = binding.edittextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.signInWithEmail(email, password)
            } else {
                Toast.makeText(activity, getString(R.string.login_not_filled), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSignUp.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                hide(this@LoginFragment)
                add(R.id.layout_fragment_container, SignUpFragment())
                addToBackStack(null)
                commit()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}