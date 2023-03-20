package com.givebox.ui.register

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
import com.givebox.common.*
import com.givebox.data.enums.AuthenticationType
import com.givebox.databinding.FragmentRegisterBinding
import com.givebox.domain.model.GiveBoxUser
import com.givebox.ui.BaseFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {
    private val viewModel by viewModels<RegisterViewModel>()
    private var launchGoogleAuth: ActivityResultLauncher<Intent>? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override fun getViewBinding(): FragmentRegisterBinding {
        return FragmentRegisterBinding.inflate(layoutInflater)
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
                    val user = GiveBoxUser(
                        "",
                        account.displayName.toString(),
                        account.photoUrl.toString(),
                        account.email.toString(),
                        null,
                        null,
                        false,
                        isTerminated = false,
                        dateJoined = getCurrentDate(),
                        authenticationType = AuthenticationType.GOOGLE
                    )
                    account.idToken?.let { idToken -> viewModel.registerWithGoogle(user, idToken) }
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

        binding.btnSignup.setOnClickListener {
            viewModel.validateInputs(
                binding.edtUsername.text.toString(),
                binding.edtEmail.text.toString(),
                binding.edtPhone.text.toString(),
                binding.edtPassword.text.toString()
            )
        }

        binding.txtSignIn.setOnClickListener { findNavController().popBackStack() }
        binding.imgBack.setOnClickListener { activity?.onBackPressed() }
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
            viewModel.register.collectLatest {
                it?.let {
                    findNavController().navigateSafely(
                        R.id.registerFragment,
                        R.id.action_registerFragment_to_profileSetupFragment
                    )
                }
            }
        }

    }

}