package com.givebox.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.givebox.data.local.db.entity.ProductCategoryEntity
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.CategoryRepositoryImpl
import com.givebox.data.repository.ProductRepositoryImpl
import com.givebox.data.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Fasil on 26/11/22.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appPreferenceManager: AppPreferenceManager,
    private val userRepository: UserRepositoryImpl,
    private val productRepository: ProductRepositoryImpl,
    private val categoryRepository: CategoryRepositoryImpl
) : ViewModel() {
    private val _profile: MutableStateFlow<UserEntity?> = MutableStateFlow(null)
    val profile get() = _profile.asStateFlow()
    private val _products: MutableStateFlow<List<ProductEntity>?> = MutableStateFlow(null)
    val products get() = _products.asStateFlow()
    private val _productCategory: MutableStateFlow<List<ProductCategoryEntity>?> = MutableStateFlow(null)
    val productCategory get() = _productCategory.asStateFlow()
    private var productSelectionQuery = SimpleSQLiteQuery("SELECT * FROM Products WHERE isActive = ? ORDER BY dateAdded DESC", arrayOf(1))
    private var productCategoryEntity: ProductCategoryEntity? = null

    init {
        getProfile()
        getProducts()
        getProductCategories()
    }

    private fun getProductCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.getAllProductCategoriesFromLocal().collectLatest {
                _productCategory.emit(listOf())
                _productCategory.emit(it)
            }
        }
    }

    private fun getProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.getProducts(productSelectionQuery).collectLatest {
                _products.emit(listOf())
                _products.emit(it)
            }
        }
    }

    private fun getProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserById(appPreferenceManager.getUserId().toString()).collectLatest {
                _profile.emit(it)
            }
        }
    }

    fun setProductsQuery(simpleSQLiteQuery: SimpleSQLiteQuery) {
        productSelectionQuery = simpleSQLiteQuery
        getProducts()
    }

    fun onProductCategorySelect(categoryEntity: ProductCategoryEntity?) {
        productCategoryEntity = categoryEntity
        if (categoryEntity == null) {
            setProductsQuery(SimpleSQLiteQuery("SELECT * FROM Products WHERE isActive = ? ORDER BY dateAdded DESC", arrayOf(1)))
        } else {
            setProductsQuery(
                SimpleSQLiteQuery(
                    "SELECT * FROM Products WHERE isActive = ? AND categoryId = ? ORDER BY dateAdded DESC",
                    arrayOf(1, categoryEntity.id)
                )
            )
        }
    }

    fun onSearch(query: String) {
        if (query.isEmpty()) {
            if (productCategoryEntity == null) {
                setProductsQuery(
                    SimpleSQLiteQuery(
                        "SELECT * FROM Products WHERE isActive = ? ORDER BY dateAdded DESC",
                        arrayOf(1)
                    )
                )
            } else {
                setProductsQuery(
                    SimpleSQLiteQuery(
                        "SELECT * FROM Products WHERE isActive = ? AND categoryId = ? ORDER BY dateAdded DESC",
                        arrayOf(1, productCategoryEntity?.id)
                    )
                )
            }
        } else {
            if (productCategoryEntity == null) {
                setProductsQuery(
                    SimpleSQLiteQuery(
                        "SELECT * FROM Products WHERE isActive = ? AND name LIKE '%' || ? || '%' ORDER BY dateAdded DESC",
                        arrayOf(1, query)
                    )
                )
            } else {
                setProductsQuery(
                    SimpleSQLiteQuery(
                        "SELECT * FROM Products WHERE isActive = ? AND name LIKE '%' || ? || '%' AND categoryId = ? ORDER BY dateAdded DESC",
                        arrayOf(1, query, productCategoryEntity?.id)
                    )
                )
            }
        }
    }

    fun likeProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.likeProduct(productId, appPreferenceManager.getUserId().toString())
        }
    }

    fun unLikeProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.unLikeProduct(productId, appPreferenceManager.getUserId().toString())
        }
    }

}