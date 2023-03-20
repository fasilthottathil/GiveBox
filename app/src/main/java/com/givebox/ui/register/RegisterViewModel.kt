package com.givebox.ui.register

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.givebox.common.*
import com.givebox.data.enums.AuthenticationType
import com.givebox.data.repository.UserRepositoryImpl
import com.givebox.data.worker.DataSyncWorker
import com.givebox.domain.model.GiveBoxUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by Fasil on 24/11/22.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val application: Application
) : ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val _register: MutableStateFlow<Unit?> = MutableStateFlow(null)
    val register get() = _register.asStateFlow()

    private fun registerWithPassword(giveBoxUser: GiveBoxUser) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.send(true)
            when (val result = userRepository.registerWithEmailAndPassword(
                giveBoxUser.email.toString(),
                giveBoxUser.password.toString()
            )) {
                is Resource.Success -> {
                    registerUser(giveBoxUser.apply { this.id = result.data })
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(result.message)
                }
            }
        }
    }

    private fun registerUser(giveBoxUser: GiveBoxUser) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = userRepository.registerUser(giveBoxUser)) {
                is Resource.Success -> {
                    startSyncWorker()
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(result.message)
                }
            }
        }
    }

    private fun startSyncWorker() {
        viewModelScope.launch(Dispatchers.IO) {
            val worker = OneTimeWorkRequestBuilder<DataSyncWorker>().apply {
                addTag(DataSyncWorker::class.java.simpleName)
            }.build()
            val workManager = WorkManager.getInstance(application)
            workManager.enqueue(worker)
            workManager.getWorkInfoByIdLiveData(worker.id).asFlow().collectLatest {
                it?.let {
                    if (it.state == WorkInfo.State.SUCCEEDED) {
                        _loading.send(false)
                        _register.emit(Unit)
                    } else if (it.state == WorkInfo.State.FAILED) {
                        _loading.send(false)
                        _error.send("An error occurred")
                    }
                }
            }
        }
    }

    fun registerWithGoogle(giveBoxUser: GiveBoxUser, idToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.send(true)
            when (val response = userRepository.registerWithGoogle(idToken)) {
                is Resource.Success -> {
                    registerUser(giveBoxUser.apply { id = response.data })
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(response.message)
                }
            }
        }
    }

    fun validateInputs(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            name.validateInputField("Username").also {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                }
            }
            email.validateEmail().also {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                }
            }
            phone.validatePhone().also {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                }
            }
            password.validatePassword().also {
                if (!it.first) {
                    _error.send(it.second)
                    return@launch
                }
            }
            registerWithPassword(
                GiveBoxUser(
                    UUID.randomUUID().toString(),
                    name,
                    email,
                    phone,
                    password,
                    null,
                    false,
                    isTerminated = false,
                    dateJoined = getCurrentDate(),
                    authenticationType = AuthenticationType.PASSWORD
                )
            )
        }


    }

}


