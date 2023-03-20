package com.givebox.ui.post

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.givebox.R
import com.givebox.common.*
import com.givebox.databinding.FragmentAddPostBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AddPostFragment : BaseFragment<FragmentAddPostBinding>() {
    private val viewModel by viewModels<AddPostViewModel>()
    private val imageUriList = arrayListOf<Uri?>()
    @Inject lateinit var requestManager: RequestManager
    override fun getViewBinding(): FragmentAddPostBinding {
        return FragmentAddPostBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResults {
            it?.let {
                imageUriList.add(it.data)
                requestManager.load(imageUriList[0])
                    .circleCrop()
                    .into(binding.imgProduct)
                populateImages()
            }
        }
    }

    private fun populateImages() {
        binding.imgProduct1.setImageDrawable(null)
        binding.imgProduct2.setImageDrawable(null)
        binding.imgProduct3.setImageDrawable(null)
        binding.imgProduct4.setImageDrawable(null)
        binding.imgProduct5.setImageDrawable(null)
        for (i in imageUriList.indices) {
            when(i) {
                0 -> {
                    requestManager.load(imageUriList[0]).circleCrop().into(binding.imgProduct)
                    requestManager.load(imageUriList[0])
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                        .into(binding.imgProduct1)
                }
                1 -> requestManager.load(imageUriList[1])
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(binding.imgProduct2)
                2 -> requestManager.load(imageUriList[2])
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(binding.imgProduct3)
                3 -> requestManager.load(imageUriList[3])
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(binding.imgProduct4)
                4 -> requestManager.load(imageUriList[4])
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(binding.imgProduct5)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        binding.imgProduct.setOnClickListener {
            if (imageUriList.size == 5) {
                showDialog(message = "You can only upload up to 5 images")
            } else {
                selectImage()
            }
        }

        binding.imgProduct1.setOnClickListener {
            selectOrDeleteImage(1)
        }

        binding.imgProduct2.setOnClickListener {
           selectOrDeleteImage(2)
        }

        binding.imgProduct3.setOnClickListener {
            selectOrDeleteImage(3)
        }

        binding.imgProduct4.setOnClickListener {
            selectOrDeleteImage(4)
        }

        binding.imgProduct5.setOnClickListener {
            selectOrDeleteImage(5)
        }

        binding.txtProductType.setOnClickListener {
            context.showListAlertDialog("Select Product Type", arrayOf("Giveaway", "Exchange", "Giveaway Or Exchange")) {
                binding.txtProductType.text = it
                binding.txtProductType.setTextColor(Color.BLACK)
            }
        }

        binding.txtProductAge.setOnClickListener {
            context.showListAlertDialog("Select Product Age", arrayOf("1 month", "3 month", "Above 6 month", "1 year", "2 year", "3 year", "5 year", "Above 5 year")) {
                binding.txtProductAge.text = it
                binding.txtProductAge.setTextColor(Color.BLACK)
            }
        }

        binding.txtCategory.setOnClickListener {
            context.showListAlertDialog("Select Product Category", viewModel.productCategoryNameAndIdList.toTypedArray()) {
                binding.txtCategory.text = it
                binding.txtCategory.setTextColor(Color.BLACK)
            }
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.validateInputs(
                imageUriList,
                binding.edtProductName.text.toString(),
                binding.edtProductDesc.text.toString(),
                binding.txtCategory.text.toString(),
                binding.txtProductType.text.toString(),
                binding.txtProductAge.text.toString()
            )
        }

        binding.imgBack.setOnClickListener { activity?.onBackPressed() }
    }

    private fun selectOrDeleteImage(position: Int) {
        if (imageUriList.size >= position) {
            context.showQuestionDialog("Do you want to delete this image", "Yes", "No") {
                if (it) {
                    imageUriList.removeAt(position - 1)
                    if (position == 1)  requestManager.load(R.drawable.ic_add).into(binding.imgProduct)
                   populateImages()
                }
            }
        } else {
            selectImage()
        }
    }

    private fun selectImage() {
        launchActivityResult(Intent().apply {
            this.action = Intent.ACTION_GET_CONTENT
            this.type = "image/*"
        })
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
            viewModel.addProduct.collectLatest {
                it?.let {
                    showDialog("Success","Product added successfully")
                    resetViews()
                }
            }
        }

    }

    private fun resetViews() {
        binding.imgProduct1.setImageDrawable(null)
        binding.imgProduct1.setImageDrawable(null)
        binding.imgProduct2.setImageDrawable(null)
        binding.imgProduct3.setImageDrawable(null)
        binding.imgProduct4.setImageDrawable(null)
        binding.imgProduct5.setImageDrawable(null)

        binding.edtProductName.text?.clear()
        binding.edtProductDesc.text?.clear()
        binding.txtCategory.text = getString(R.string.select_category)
        binding.txtProductAge.text = getString(R.string.select_age)
        binding.txtProductType.text = getString(R.string.give_or_exchange)

        context?.let {
            binding.txtCategory.setTextColor(ContextCompat.getColor(it, R.color.gray))
            binding.txtProductAge.setTextColor(ContextCompat.getColor(it, R.color.gray))
            binding.txtProductType.setTextColor(ContextCompat.getColor(it, R.color.gray))
        }
    }

}