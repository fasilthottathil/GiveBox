package com.givebox.ui.favourite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.givebox.databinding.FragmentFavouriteBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class FavouriteFragment : BaseFragment<FragmentFavouriteBinding>() {
    private val viewModel by viewModels<FavouriteViewModel>()
    @Inject lateinit var requestManager: RequestManager
    private val favouriteProductsAdapter by lazy { FavouriteProductsAdapter(requestManager) }
    override fun getViewBinding(): FragmentFavouriteBinding {
        return FragmentFavouriteBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        binding.rvFavourite.apply {
            adapter = favouriteProductsAdapter.also {
                it.setOnDislikeClickListener { productEntity ->
                    viewModel.unLikeProduct(productEntity.id)
                }
            }
        }

        binding.imgBack.setOnClickListener { activity?.onBackPressed() }
    }

    override fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.products.collectLatest {
                favouriteProductsAdapter.submitList(it)
            }
        }
    }

}