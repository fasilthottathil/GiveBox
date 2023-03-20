package com.givebox.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.common.Resource
import com.givebox.common.validatePhone
import com.givebox.data.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Fasil on 30/11/22.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl
): ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val _forgotPass: MutableStateFlow<String?> = MutableStateFlow(null)
    val forgotPass get() = _forgotPass.asStateFlow()

    fun getPasswordByPhone(phone: String) {
        viewModelScope.launch (Dispatchers.IO){
            _loading.send(true)
            when(val result = userRepository.getPasswordByPhoneNumber(phone)) {
                is Resource.Success -> {
                    _loading.send(false)
                    _forgotPass.emit(result.data)
                    delay(400)
                    _forgotPass.emit(null)
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(result.message)
                }
            }
        }
    }

    fun validatePhone(phone: String?): Boolean {
        return phone.validatePhone().first
    }

}