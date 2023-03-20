package com.givebox.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.givebox.R
import com.givebox.common.*
import com.givebox.data.enums.AuthenticationType
import com.givebox.databinding.FragmentSettingsBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    private val viewModel by viewModels<SettingsViewModel>()
    private var imageUri: Uri? = null

    @Inject
    lateinit var requestManager: RequestManager
    private var authenticationType = AuthenticationType.PASSWORD
    override fun getViewBinding(): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResults {
            it?.let {
                imageUri = it.data
                requestManager.load(imageUri)
                    .circleCrop()
                    .into(binding.imgProfile)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        binding.btnUpdate.setOnClickListener {
            if (authenticationType == AuthenticationType.PASSWORD) {
                if (!binding.edtEmail.text.toString().validateEmail().first) {
                    showDialog(message = "Enter valid email")
                } else if (!binding.edtPassword.text.toString().validatePassword().first) {
                    showDialog(message = "Enter valid password")
                } else if (binding.edtUsername.text.isNullOrEmpty()) {
                    showDialog(message = "Enter valid username")
                } else {
                    viewModel.updateUser(
                        imageUri,
                        binding.edtUsername.text.toString(),
                        binding.edtPassword.text.toString(),
                        binding.edtEmail.text.toString(),
                        authenticationType
                    )
                }
            } else {
                if (binding.edtUsername.text.isNullOrEmpty()) {
                    showDialog(message = "Enter valid username")
                } else {
                    viewModel.updateUser(
                        imageUri,
                        binding.edtUsername.text.toString(),
                        null,
                        null,
                        authenticationType
                    )
                }
            }
        }

        binding.imgProfile.setOnClickListener {
            launchActivityResult(Intent().apply {
                this.action = Intent.ACTION_GET_CONTENT
                this.type = "image/*"
            })
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
            viewModel.user.collectLatest {
                it?.let {
                    authenticationType = it.authenticationType
                    if (it.authenticationType == AuthenticationType.GOOGLE) {
                        binding.layoutEmail.visibility = View.GONE
                        binding.layoutPassword.visibility = View.GONE
                    }
                    binding.edtEmail.setText(it.email)
                    binding.edtPassword.setText(it.password)
                    binding.edtUsername.setText(it.name)
                    requestManager.load(it.profileUrl)
                        .circleCrop()
                        .error(R.drawable.ic_profile_placeholder)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(binding.imgProfile)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.user.collectLatest {
                viewModel.update.collectLatest {
                    it?.let {
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }
}