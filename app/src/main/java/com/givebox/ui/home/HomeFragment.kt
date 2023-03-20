package com.givebox.ui.home

import android.Manifest
import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.sqlite.db.SimpleSQLiteQuery
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.common.checkPermissions
import com.givebox.common.getNavigationResult
import com.givebox.common.navigateSafely
import com.givebox.common.toObjectByGson
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.FragmentHomeBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel by viewModels<HomeViewModel>()

    @Inject lateinit var requestManager: RequestManager
    @Inject lateinit var application: Application
    @Inject lateinit var appPreferenceManager: AppPreferenceManager
    private val productAdapter by lazy { ProductAdapter(requestManager, appPreferenceManager) }
    private val productCategoryAdapter by lazy {
        ProductCategoryAdapter(
            requestManager,
            application
        )
    }
    override fun getViewBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context.checkPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            if (it) {
                Toast.makeText(context, "Ads are based on location", Toast.LENGTH_SHORT).show()
            } else {
                activity?.let { fragmentActivity ->
                    ActivityCompat.requestPermissions(
                        fragmentActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        2
                    )
                }
            }
        }

        initViews()
        observe()

        getNavigationResult<Pair<String,Array<Any>>>(R.id.homeFragment,"query") {
            binding.imgFilter.imageTintList = ColorStateList.valueOf(Color.RED)
            var query = "SELECT * FROM Products WHERE isActive = ? "
            val queryArray = mutableListOf<Any>()
            queryArray.add(1)
            if (it.second[1] != "null") {
                query += "AND categoryId = ? "
                queryArray.add(it.second[1])
            }
            if (it.second[2] != "null") {
                query += "AND productType = ? "
                queryArray.add(it.second[2])
            }
            if (it.second[3] != "null") {
                query += "AND age = ? "
                queryArray.add(it.second[3])
            }
            viewModel.setProductsQuery(SimpleSQLiteQuery(query,queryArray.toTypedArray()))
        }

    }

    override fun initViews() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter.also {
                it.setOnProductClickListener { productEntity ->
                    findNavController().navigateSafely(
                        R.id.homeFragment,
                        R.id.action_homeFragment_to_postDetailsFragment,
                        bundleOf("productId" to productEntity.id)
                    )
                }
                it.setOnLikeClickListener { productEntity, isLiked ->
                    if (isLiked) {
                        viewModel.likeProduct(productEntity.id)
                    } else {
                        viewModel.unLikeProduct(productEntity.id)
                    }
                }
            }
        }

        binding.rvCategory.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            adapter = productCategoryAdapter.also {
                it.setOnProductCategorySelectListener { categoryEntity ->
                    binding.imgFilter.imageTintList = ColorStateList.valueOf(Color.GRAY)
                    viewModel.onProductCategorySelect(categoryEntity)
                }
            }
        }

        binding.imgFilter.setOnClickListener {
            findNavController().navigateSafely(
                R.id.homeFragment,
                R.id.action_homeFragment_to_filterPostFragment
            )
        }

        binding.edtSearch.addTextChangedListener {
            binding.imgFilter.imageTintList = ColorStateList.valueOf(Color.GRAY)
            viewModel.onSearch(binding.edtSearch.text.toString())
        }

    }

    override fun observe() {

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.profile.collectLatest {
                it?.let {
                    requestManager.load(it.profileUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.imgProfile)
                    binding.txtUsername.text = buildSpannedString {
                        append("Welcome,\n")
                        bold {
                            context?.let { mContext ->
                                color(ContextCompat.getColor(mContext, R.color.black)) {
                                    bold { append(it.name) }
                                }
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.products.collectLatest {
                productAdapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.productCategory.collectLatest {
                productCategoryAdapter.submitList(it)
            }
        }

    }

}