package com.givebox.ui.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.ProductRepositoryImpl
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
class FavouriteViewModel @Inject constructor(
    private val productRepository: ProductRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
): ViewModel() {
    private val _products: MutableStateFlow<List<ProductEntity>?> = MutableStateFlow(null)
    val products get() = _products.asStateFlow()

    init {
        getLikedProducts()
    }

    private fun getLikedProducts() {
        viewModelScope.launch (Dispatchers.IO){
            productRepository.getAllProducts().collectLatest {
                val list = it.filter { entity ->
                    if (entity.likes.isNotEmpty()) {
                        val likeMap =
                            entity.likes.replace("[{}]".toRegex(), "").split(",").associate { str ->
                                val (left, right) = str.split("=")
                                left to right
                            }
                        if (likeMap.containsKey(appPreferenceManager.getUserId())) {
                            likeMap[appPreferenceManager.getUserId().toString()].toString() == "true"
                        } else false
                    } else false
                }
                _products.emit(list)
            }
        }
    }

    fun unLikeProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.unLikeProduct(productId, appPreferenceManager.getUserId().toString())
        }
    }

}