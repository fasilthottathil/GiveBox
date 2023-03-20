package com.givebox.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.common.Resource
import com.givebox.data.local.db.entity.ChatsEntity
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.ChatRepositoryImpl
import com.givebox.data.repository.ProductRepositoryImpl
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
class PostDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepositoryImpl,
    private val chatRepository: ChatRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val _product: MutableStateFlow<ProductEntity?> = MutableStateFlow(null)
    val product get() = _product.asStateFlow()
    var isLiked = false
    private var productId: String? = null
    private val _startChat: MutableStateFlow<ChatsEntity?> = MutableStateFlow(null)
    val startChat get() = _startChat.asStateFlow()

    fun getProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.getProductById(productId).collectLatest {
                it?.let {
                    this@PostDetailsViewModel.productId = it.id
                    isLiked = if (it.likes.isNotEmpty()) {
                        val likeMap = it.likes.replace("[{}]".toRegex(), "").split(",").associate { str ->
                            val (left, right) = str.split("=")
                            left to right
                        }
                        if (likeMap.containsKey(appPreferenceManager.getUserId())) {
                            likeMap[appPreferenceManager.getUserId()] == "true"
                        } else false
                    } else false
                }
                _product.emit(it)
            }
        }
    }

    fun likeOrUnlikeProduct() {
        viewModelScope.launch(Dispatchers.IO) {
            productId?.let {
                if (isLiked)
                    productRepository.unLikeProduct(it, appPreferenceManager.getUserId().toString())
                else
                    productRepository.likeProduct(it, appPreferenceManager.getUserId().toString())
            }
        }
    }

    fun startChat(userEntity: UserEntity) {
        viewModelScope.launch (Dispatchers.IO){
            _loading.send(true)
            val chat = chatRepository.startChatFromLocal(userEntity)
            if (chat == null) {
                when(val result = chatRepository.startChat(userEntity)) {
                    is Resource.Success -> {
                        _loading.send(false)
                        _startChat.emit(result.data)
                        delay(400)
                        _startChat.emit(null)
                    }
                    is Resource.Error -> {
                        _loading.send(false)
                        _error.send(result.message)
                    }
                }
            } else {
                _loading.send(false)
                _startChat.emit(chat)
                delay(400)
                _startChat.emit(null)
            }
        }
    }

}