package com.givebox.ui.post

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.sqlite.db.SimpleSQLiteQuery
import com.bumptech.glide.RequestManager
import com.givebox.common.*
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.FragmentFilterPostBinding
import com.givebox.ui.BaseFragment
import com.givebox.ui.home.ProductCategoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class FilterPostFragment : DialogFragment() {
    private val viewModel by viewModels<FilterPostViewModel>()

    @Inject
    lateinit var application: Application

    @Inject
    lateinit var requestManager: RequestManager

    @Inject
    lateinit var appPreferenceManager: AppPreferenceManager
    private val productCategoryAdapter by lazy {
        ProductCategoryAdapter(
            requestManager,
            application
        )
    }
    private var categoryId: String? = null
    private var productAge: String? = null
    private var productType: String? = null
    private var _binding: FragmentFilterPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterPostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScreenSizeAwareWidth(90)
        initViews()
        observe()
    }

    private fun initViews() {

        binding.rvCategory.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            adapter = productCategoryAdapter.also {
                it.setOnProductCategorySelectListener { categoryEntity ->
                    categoryId = categoryEntity?.id
                }
            }
        }

        binding.txtProductAge.setOnClickListener {
            context.showListAlertDialog("Select Product Age", arrayOf("1 month", "3 month", "Above 6 month", "1 year", "2 year", "3 year", "5 year", "Above 5 year")) {
                binding.txtProductAge.text = it
                binding.txtProductAge.setTextColor(Color.BLACK)
                productAge = it
            }
        }

        binding.txtProductType.setOnClickListener {
            context.showListAlertDialog("Select Product Type", arrayOf("Give", "Exchange", "Give Or Exchange")) {
                binding.txtProductType.text = it
                binding.txtProductType.setTextColor(Color.BLACK)
                productType = it
            }
        }

        binding.btnApplyFilter.setOnClickListener {
            if (categoryId == null && productType == null && productAge == null) {
                showDialog(message = "Select at least one filter options to continue!")
            } else {
                setNavigationResult(
                    "query",
                    Pair(
                        "SELECT * FROM Products WHERE isActive = ? AND categoryId = ? AND categoryId = ? AND productType = ? AND age = ?",
                        arrayOf(1, categoryId.toString(), productType.toString(), productAge.toString())
                    )
                )
                findNavController().popBackStack()
            }
        }


        binding.imgBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.productCategory.collectLatest {
                productCategoryAdapter.submitList(it)
            }
        }
    }
}