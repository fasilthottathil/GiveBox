package com.givebox.ui.post

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.common.*
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.FragmentPostDetailsBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailsFragment : BaseFragment<FragmentPostDetailsBinding>() {
    private val viewModel by viewModels<PostDetailsViewModel>()
    @Inject lateinit var requestManager: RequestManager
    @Inject lateinit var appPreferenceManager: AppPreferenceManager
    private val imagesAdapter by lazy { ImagesAdapter(requestManager) }
    private val navArgs by navArgs<PostDetailsFragmentArgs>()
    private var userEntity: UserEntity? = null
    override fun getViewBinding(): FragmentPostDetailsBinding {
        return FragmentPostDetailsBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        binding.rvImages.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            adapter = imagesAdapter
        }

        binding.imgFavourite.setOnClickListener {
            viewModel.likeOrUnlikeProduct()
        }

        binding.btnChat.setOnClickListener {
            userEntity?.let { viewModel.startChat(it) }
        }

        binding.imgBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun observe() {

        navArgs.productId?.let { viewModel.getProduct(it) }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.product.collectLatest {
                it?.let {
                    if (viewModel.isLiked) {
                        binding.imgFavourite.imageTintList = ColorStateList.valueOf(Color.RED)
                    } else {
                        binding.imgFavourite.imageTintList = ColorStateList.valueOf(Color.GRAY)
                    }
                    imagesAdapter.submitList(it.images.toString().replace("[\\[\\]]".toRegex(),"").split(","))
                    binding.txtProduct.text = it.name
                    binding.txtProductDesc.text = it.description
                    userEntity = it.user.toString().toObjectByGson<UserEntity>()
                    "Uploaded by\n${userEntity?.name}".also { uploadedBy -> binding.txtUsername.text = uploadedBy }
                    if (userEntity?.id == appPreferenceManager.getUserId()) {
                        binding.layoutBottom.visibility = View.GONE
                    }
                    requestManager.load(userEntity?.profileUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.imgProfile)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.startChat.collectLatest {
                it?.let {
                    findNavController().navigateSafely(
                        R.id.postDetailsFragment,
                        R.id.action_postDetailsFragment_to_privateChatFragment,
                        bundleOf(
                            "roomId" to it.roomId,
                            "userId" to it.userId,
                            "user" to userEntity.toGsonByObject()
                        )
                    )
                }
            }
        }

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

    }
}