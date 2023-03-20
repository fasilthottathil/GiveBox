package com.givebox.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.data.local.db.entity.ChatsEntity
import com.givebox.data.repository.ChatRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Fasil on 29/11/22.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepositoryImpl
) : ViewModel() {
    private val _chats: MutableStateFlow<List<ChatsEntity>?> = MutableStateFlow(null)
    val chats get() = _chats.asStateFlow()

    init {
        getChatsFromLocal()
        getChatsFromServer()
    }

    private fun getChatsFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getChatsFromServer()
        }
    }

    private fun getChatsFromLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getChatsFromLocal().collectLatest {
                _chats.emit(it)
            }
        }
    }

}