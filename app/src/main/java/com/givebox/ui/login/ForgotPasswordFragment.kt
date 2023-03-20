package com.givebox.ui.login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.givebox.R
import com.givebox.common.*
import com.givebox.databinding.FragmentForgotPasswordBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ForgotPasswordFragment: BaseFragment<FragmentForgotPasswordBinding>(){
    private val viewModel by viewModels<ForgotPasswordViewModel>()
    private var phone = ""
    override fun getViewBinding(): FragmentForgotPasswordBinding {
        return FragmentForgotPasswordBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {
        binding.btnVerify.setOnClickListener {
            if (viewModel.validatePhone(binding.edtPhone.text.toString())) {
                phone = binding.edtPhone.text.toString()
                findNavController().navigateSafely(
                    R.id.forgotPasswordFragment,
                    R.id.action_forgotPasswordFragment_to_otpFragment,
                    bundleOf("phone" to binding.edtPhone.text.toString())
                )
            } else {
                showDialog(message = getString(R.string.invalid_phone))
            }
        }

        binding.imgBack.setOnClickListener { findNavController().popBackStack() }
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
            viewModel.forgotPass.collectLatest {
                it?.let {
                    context.showQuestionDialog("Your password is : $it", "Copy Password", "Close") { isCopy ->
                        if (isCopy) {
                            val clipBoard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipBoard.setPrimaryClip(ClipData.newPlainText(null,it))
                            Toast.makeText(context,"Password copied to clipboard",Toast.LENGTH_SHORT).show()
                            this@ForgotPasswordFragment.findNavController().popBackStack()
                        }
                    }
                }
            }
        }

        getNavigationResult<Boolean>(R.id.forgotPasswordFragment,"isVerified"){
            if (it) {
                viewModel.getPasswordByPhone(phone)
            }
        }
    }

}