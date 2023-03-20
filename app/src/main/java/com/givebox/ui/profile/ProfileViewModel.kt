package com.givebox.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.common.Resource
import com.givebox.common.toObjectByGson
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.ProductRepositoryImpl
import com.givebox.data.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Fasil on 27/11/22.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val productRepository: ProductRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val _user: MutableStateFlow<UserEntity?> = MutableStateFlow(null)
    val user get() = _user.asStateFlow()
    private val _products: MutableStateFlow<List<ProductEntity>?> = MutableStateFlow(null)
    val products get() = _products.asStateFlow()
    private val _deleteProduct: MutableStateFlow<Unit?> = MutableStateFlow(null)
    val deleteProduct get() = _deleteProduct.asStateFlow()

    init {
        getUser()
        getMyProducts()
    }

    private fun getUser() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserById(appPreferenceManager.getUserId().toString()).collectLatest {
                _user.emit(it)
            }
        }
    }

    private fun getMyProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.getAllProducts().collectLatest {
                it.filter { entity ->
                    val user = entity.user.toString().toObjectByGson<UserEntity>()
                    user.id == appPreferenceManager.getUserId()
                }.also { entityList ->
                    _products.emit(entityList)
                }
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch (Dispatchers.IO){
            _loading.send(true)
            when(val result = productRepository.deleteProductFromServer(productId)) {
                is Resource.Success -> {
                    _loading.send(false)
                    _deleteProduct.emit(Unit)
                    delay(400)
                    _deleteProduct.emit(null)
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(result.message)
                }
            }
        }
    }

}