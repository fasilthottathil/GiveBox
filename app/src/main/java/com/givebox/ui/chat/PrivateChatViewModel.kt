package com.givebox.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.common.toGsonByObject
import com.givebox.data.local.db.entity.PrivateChatsEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.ChatRepositoryImpl
import com.givebox.data.repository.FirebaseStorageRepositoryImpl
import com.givebox.data.repository.UserRepositoryImpl
import com.givebox.domain.model.PrivateChats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Created by Fasil on 29/11/22.
 */
@HiltViewModel
class PrivateChatViewModel @Inject constructor(
    private val chatRepository: ChatRepositoryImpl,
    private val userRepository: UserRepositoryImpl,
    private val firebaseStorageRepository: FirebaseStorageRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private var userId: String? = null
    private var roomId: String? = null
    private val _privateChats: MutableStateFlow<List<PrivateChatsEntity>?> = MutableStateFlow(null)
    val privateChats get() = _privateChats.asStateFlow()

    init {
        getPrivateChatsFromLocal()
        getPrivateChatsFromServer()
    }

    private fun getPrivateChatsFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            roomId?.let {
                chatRepository.getPrivateChatsFromServer(it)
            }
        }
    }

    private fun getPrivateChatsFromLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            roomId?.let {
                chatRepository.getPrivateChatsFromLocal(it).collectLatest { privateChatsEntities ->
                    _privateChats.emit(privateChatsEntities)
                }
            }
        }
    }

    fun sendMessage(privateChats: PrivateChats) {
        viewModelScope.launch(Dispatchers.IO) {
            roomId?.let { roomId ->
                userId?.let { userId ->
                    chatRepository.sendMessage(privateChats.apply {
                        this.id = UUID.randomUUID().toString()
                        this.roomId = roomId
                        this.user = userRepository.getUser(appPreferenceManager.getUserId().toString()).toGsonByObject()
                        this.status = "Active"
                        this.timestamp = System.currentTimeMillis()
                    }, userId)
                }
            }
        }
    }

    fun uploadAndSendMessage(privateChats: PrivateChats, uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.send(true)
            firebaseStorageRepository.uploadFile(
                uri,
                "chats/private/$roomId/${System.currentTimeMillis()}",
                {
                    viewModelScope.launch {
                        _loading.send(false)
                        sendMessage(privateChats.apply { this.message = it })
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


    fun setUserIdAndRoomId(userId: String, roomId: String) {
        this.userId = userId
        this.roomId = roomId
    }
}