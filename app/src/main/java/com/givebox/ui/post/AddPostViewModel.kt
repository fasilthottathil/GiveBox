package com.givebox.ui.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.common.Resource
import com.givebox.common.getCurrentDate
import com.givebox.common.toGsonByObject
import com.givebox.common.validateInputField
import com.givebox.data.local.db.entity.ProductCategoryEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.CategoryRepositoryImpl
import com.givebox.data.repository.FirebaseStorageRepositoryImpl
import com.givebox.data.repository.ProductRepositoryImpl
import com.givebox.data.repository.UserRepositoryImpl
import com.givebox.domain.model.GiveBoxProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Created by Fasil on 27/11/22.
 */
@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val categoryRepository: CategoryRepositoryImpl,
    private val productRepository: ProductRepositoryImpl,
    private val userRepository: UserRepositoryImpl,
    private val firebaseStorageRepository: FirebaseStorageRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val productCategory: MutableStateFlow<List<ProductCategoryEntity>?> = MutableStateFlow(null)
    var productCategoryNameAndIdList = mutableListOf<String>()
    private var giveBoxProduct: GiveBoxProduct? = null
    private val _addProduct: MutableStateFlow<Unit?> = MutableStateFlow(null)
    val addProduct get() = _addProduct.asStateFlow()

    init {
        getProductCategories()
    }

    private fun getProductCategories() {
        viewModelScope.launch(Dispatchers.IO){
            categoryRepository.getAllProductCategoriesFromLocal().collectLatest {
                it.forEach { categoryEntity ->
                    productCategoryNameAndIdList.add(categoryEntity.name.toString() + "\nId:" + categoryEntity.id)
                }
            }
        }
    }

    fun validateInputs(
        imageList: List<Uri?>,
        productName: String,
        productDescription: String,
        productCategory: String,
        productType: String,
        productAge: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (imageList.isEmpty()) {
                _error.send("Select at least one product image to continue")
                return@launch
            }
            productName.validateInputField("Product name").let {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                }
            }
            productDescription.validateInputField("Product description").let {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                }
            }
            productCategory.validateInputField("Product category").let {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                } else {
                    if (productCategory == "Select Category") {
                        _error.send("Select product category")
                        return@launch
                    }
                }
            }
            productAge.validateInputField("Product age").let {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                } else {
                    if (productAge == "Select Age") {
                        _error.send("Select product age")
                        return@launch
                    }
                }
            }
            val user = userRepository.getUser(appPreferenceManager.getUserId().toString()).toGsonByObject()
            giveBoxProduct = GiveBoxProduct(
                UUID.randomUUID().toString(),
                productName,
                null,
                productCategory.split("\n")[1].replace("Id:", ""),
                user,
                productDescription,
                getCurrentDate(),
                true,
                "",
                productAge,
                productType
            )
            _loading.send(true)
            addImages(imageList)
        }
    }

    private fun addImages(imageList: List<Uri?>) {
        val imageUrlList = arrayListOf<String>()
        viewModelScope.launch (Dispatchers.IO){
            imageList.forEach {
                firebaseStorageRepository.uploadFile(
                    it,
                    "uploads/products/${getCurrentDate()}/${System.currentTimeMillis()}",
                    { url ->
                        imageUrlList.add(url)
                        if (imageUrlList.size == imageList.size) {
                            addProduct(giveBoxProduct.apply { this?.images = imageUrlList.toString() })
                        }
                    },
                    {},
                    {
                       viewModelScope.launch {
                           _loading.send(false)
                           _error.send(it)
                       }
                    })
            }
        }
    }

    private fun addProduct(product: GiveBoxProduct?) {
        viewModelScope.launch(Dispatchers.IO){
            product?.let {
                when(val result = productRepository.insertProduct(product)) {
                    is Resource.Success -> {
                        _loading.send(false)
                        _addProduct.emit(Unit)
                        delay(400)
                        _addProduct.emit(null)
                    }
                    is Resource.Error -> {
                        _loading.send(false)
                        _error.send(result.message)
                    }
                }
            }
        }
    }

}