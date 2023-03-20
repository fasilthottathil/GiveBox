package com.givebox.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.common.Resource
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.data.repository.FirebaseStorageRepositoryImpl
import com.givebox.data.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Fasil on 26/11/22.
 */
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val firebaseStorageRepository: FirebaseStorageRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
) : ViewModel() {

    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val _onProfileSet: MutableStateFlow<Unit?> = MutableStateFlow(null)
    val onProfileSet get() = _onProfileSet.asStateFlow()

    fun setProfilePicture(profileUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.send(true)
            firebaseStorageRepository.uploadFile(
                profileUri,
                "uploads/profile/image/${appPreferenceManager.getUserId()}",
                {
                    updateProfilePicture(it)
                },
                {}, {
                    viewModelScope.launch {
                        _loading.send(false)
                        _error.send(it)
                    }
                })
        }
    }

    private fun updateProfilePicture(profileUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = userRepository.updateProfilePicture(
                appPreferenceManager.getUserId().toString(),
                profileUrl
            )) {
                is Resource.Success -> {
                    _loading.send(false)
                    _onProfileSet.emit(Unit)
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(result.message)
                }
            }
        }
    }

}