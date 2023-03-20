package com.givebox.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.data.local.db.entity.ProductCategoryEntity
import com.givebox.data.repository.CategoryRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Fasil on 27/11/22.
 */
@HiltViewModel
class FilterPostViewModel @Inject constructor(
    private val categoryRepository: CategoryRepositoryImpl
) : ViewModel() {
    private val _productCategory: MutableStateFlow<List<ProductCategoryEntity>?> = MutableStateFlow(null)
    val productCategory get() = _productCategory.asStateFlow()

    init {
        getProductCategories()
    }

    private fun getProductCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.getAllProductCategoriesFromLocal().collectLatest {
                _productCategory.emit(it)
            }
        }
    }
}