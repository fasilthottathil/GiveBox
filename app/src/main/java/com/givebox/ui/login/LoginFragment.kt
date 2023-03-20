package com.givebox.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.givebox.R
import com.givebox.common.hideLoading
import com.givebox.common.navigateSafely
import com.givebox.common.showDialog
import com.givebox.common.showLoading
import com.givebox.databinding.FragmentLoginBinding
import com.givebox.ui.BaseFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private val viewModel by viewModels<LoginViewModel>()
    private var launchGoogleAuth: ActivityResultLauncher<Intent>? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override fun getViewBinding(): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchGoogleAuth = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    val account = task.getResult(ApiException::class.java)
                    account.idToken?.let { idToken -> viewModel.loginWithGoogle(idToken) }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()

        context?.let { mGoogleSignInClient = GoogleSignIn.getClient(it, gso) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        binding.btnGoogle.setOnClickListener {
            launchGoogleAuth?.launch(mGoogleSignInClient?.signInIntent)
        }

        binding.btnSignIn.setOnClickListener {
            viewModel.validateInputs(
                binding.edtEmail.text.toString(),
                binding.edtPassword.text.toString()
            )
        }

        binding.txtForgotPass.setOnClickListener {
            findNavController().navigateSafely(
                R.id.loginFragment,
                R.id.action_loginFragment_to_forgotPasswordFragment
            )
        }

        binding.txtSignUp.setOnClickListener {
            findNavController().navigateSafely(
                R.id.loginFragment,
                R.id.action_loginFragment_to_registerFragment
            )
        }
    }

    override fun observe() {

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.loading.collectLatest {
                if (it) showLoading() else hideLoading()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.error.collectLatest {
                it?.let {
                    showDialog(message = it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.login.collectLatest {
                it?.let {
                    findNavController().navigateSafely(
                        R.id.loginFragment,
                        R.id.action_loginFragment_to_mainFragment
                    )
                }
            }
        }

    }

}