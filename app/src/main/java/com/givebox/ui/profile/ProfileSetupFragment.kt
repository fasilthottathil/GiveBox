package com.givebox.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.common.*
import com.givebox.databinding.FragmentProfileSetupBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSetupFragment : BaseFragment<FragmentProfileSetupBinding>() {
    private val viewModel by viewModels<ProfileSetupViewModel>()
    private var imageUri: Uri? = null
    @Inject lateinit var requestManager: RequestManager
    override fun getViewBinding(): FragmentProfileSetupBinding {
        return FragmentProfileSetupBinding.inflate(layoutInflater)
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

        binding.imgProfile.setOnClickListener {
            launchActivityResult(Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            })
        }

        binding.btnVerify.setOnClickListener {
            if (imageUri == null) {
                showDialog(message = "Select image to continue")
            } else {
                viewModel.setProfilePicture(imageUri)
            }
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
            viewModel.onProfileSet.collectLatest {
                it?.let {
                    findNavController().navigateSafely(
                        R.id.profileSetupFragment,
                        R.id.action_profileSetupFragment_to_mainFragment
                    )
                }
            }
        }

    }

}