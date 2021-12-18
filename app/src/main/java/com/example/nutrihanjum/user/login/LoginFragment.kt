package com.example.nutrihanjum.user.login

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.FragmentLoginBinding
import com.example.nutrihanjum.databinding.LayoutPopupForgotPasswordBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel
    private lateinit var googleLoginLauncher: ActivityResultLauncher<Intent>

    private lateinit var popupForgotPassword: Dialog
    private lateinit var popupForgotPasswordBinding: LayoutPopupForgotPasswordBinding

    private val mOAuthLoginModule get() = OAuthLogin.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)

        initDialog()
        addLiveDataObserver()
        setLoginListener()

        return binding.root
    }

    private fun addLiveDataObserver() {
        viewModel.forgotResult.observe(viewLifecycleOwner, Observer {
            if(it) popupForgotPassword.dismiss()
            else Toast.makeText(activity, getString(R.string.send_email_fail), Toast.LENGTH_LONG).show()
        })
    }

    private fun initDialog() {
        popupForgotPassword = Dialog(requireContext())
        popupForgotPasswordBinding = LayoutPopupForgotPasswordBinding.inflate(LayoutInflater.from(requireContext()))
        popupForgotPassword.setContentView(popupForgotPasswordBinding.root)
        popupForgotPassword.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupForgotPasswordBinding.btnCancelSendEmail.setOnClickListener { popupForgotPassword.dismiss() }
        popupForgotPasswordBinding.btnSendEmail.setOnClickListener {
            viewModel.resetPassword(popupForgotPasswordBinding.edittextEmailForgot.text.toString())
        }
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

    private val mOAuthLoginHandler: OAuthLoginHandler = @SuppressLint("HandlerLeak")
    object: OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (success) {
                val accessToken = mOAuthLoginModule.getAccessToken(requireContext())
                Log.wtf("로그인 성공", accessToken)
                viewModel.signInWithNaver(accessToken, requireContext())
            } else {
                val errorCode = mOAuthLoginModule.getLastErrorCode(requireContext()).code
                val errorDesc = mOAuthLoginModule.getLastErrorDesc(requireContext())
                Log.wtf("로그인 실패", "errorCode:" + errorCode
                            + ", errorDesc:" + errorDesc)
            }
        }
    }

    private fun naverLogin() {
        mOAuthLoginModule.init(
            context,
            requireContext().getString(R.string.naver_client_id),
            requireContext().getString(R.string.naver_client_secret),
            requireContext().getString(R.string.app_name)
        )

        mOAuthLoginModule.startOauthLoginActivity(requireActivity(), mOAuthLoginHandler)
    }

    private fun setLoginListener() {
        googleLoginLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                try {
                    val credential = GoogleAuthProvider.getCredential(task.result.idToken, null)
                    viewModel.authWithCredential(credential)
                } catch (e: Exception) {
                    Log.wtf(activity?.localClassName, e.message)
                }
            }

        binding.btnGoogleLogin.setOnClickListener {
            googleLogin()
        }

        binding.btnKakaotalkLogin.setOnClickListener {
            viewModel.signInWithKakaotalk(requireContext())
        }

        binding.btnNaverLogin.setOnClickListener {
            naverLogin()
        }

        binding.btnEmailLogin.setOnClickListener {
            val email = binding.edittextEmail.text.toString()
            val password = binding.edittextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.signInWithEmail(email, password)
            } else {
                Toast.makeText(activity, getString(R.string.login_not_filled), Toast.LENGTH_SHORT)
                    .show()
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

        binding.btnForgotPassword.setOnClickListener {
            popupForgotPassword.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}