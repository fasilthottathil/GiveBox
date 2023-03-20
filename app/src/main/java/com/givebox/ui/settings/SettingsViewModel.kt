package com.givebox.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.common.Resource
import com.givebox.common.mapObject
import com.givebox.data.enums.AuthenticationType
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.FirebaseStorageRepositoryImpl
import com.givebox.data.repository.UserRepositoryImpl
import com.givebox.domain.model.GiveBoxUser
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
 * Created by Fasil on 07/12/22.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val firebaseStorageRepository: FirebaseStorageRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val _user: MutableStateFlow<UserEntity?> = MutableStateFlow(null)
    val user get() = _user.asStateFlow()
    private val _update: MutableStateFlow<UserEntity?> = MutableStateFlow(null)
    val update get() = _update.asStateFlow()

    init {
        getUser()
    }

    private fun getUser() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserById(appPreferenceManager.getUserId().toString()).collectLatest {
                _user.emit(it)
            }
        }
    }

    fun updateUser(imageUri: Uri?, username: String, password: String?, email: String?, authenticationType: AuthenticationType) {
        viewModelScope.launch (Dispatchers.IO){
            _loading.send(true)
            var giveBoxUser = GiveBoxUser()
            userRepository.getUser(appPreferenceManager.getUserId().toString())?.mapObject<UserEntity, GiveBoxUser>()?.let {
                giveBoxUser = it
            }
            if (authenticationType == AuthenticationType.PASSWORD) {
                giveBoxUser.password = password
                giveBoxUser.email = email
            }
            giveBoxUser.name = username
            if (imageUri == null) {
                when(val result = userRepository.updateUser(giveBoxUser)) {
                    is Resource.Success -> {
                        _loading.send(false)
                        _update.emit(result.data)
                        delay(400)
                        _update.emit(null)
                    }
                    is Resource.Error -> {
                        _loading.send(false)
                        _error.send(result.message)
                    }
                }
            } else {
                firebaseStorageRepository.uploadFile(
                    imageUri,
                    "uploads/profile/update/${System.currentTimeMillis()}",
                    {
                        giveBoxUser.profileUrl = it
                        viewModelScope.launch (Dispatchers.IO){
                            when(val result = userRepository.updateUser(giveBoxUser)) {
                                is Resource.Success -> {
                                    _loading.send(false)
                                    _update.emit(result.data)
                                    delay(400)
                                    _update.emit(null)
                                }
                                is Resource.Error -> {
                                    _loading.send(false)
                                    _error.send(result.message)
                                }
                            }
                        }
                    },
                    {},
                    {})
            }
        }
    }

}