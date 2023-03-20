package com.givebox.ui.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.givebox.common.Resource
import com.givebox.common.validateEmail
import com.givebox.common.validatePassword
import com.givebox.data.repository.UserRepositoryImpl
import com.givebox.data.worker.DataSyncWorker
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
 * Created by Fasil on 24/11/22.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val application: Application
) : ViewModel() {
    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _loading: Channel<Boolean> = Channel()
    val loading get() = _loading.receiveAsFlow()
    private val _login: MutableStateFlow<Unit?> = MutableStateFlow(null)
    val login get() = _login.asStateFlow()

    private fun loginUser(id: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = userRepository.login(id)) {
                is Resource.Success -> {
                   startSyncWorker()
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(response.message)
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.send(true)
            when (val response = userRepository.authenticateWithGoogle(idToken)) {
                is Resource.Success -> {
                    loginUser(response.data)
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(response.message)
                }
            }
        }
    }

    private fun loginWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.send(true)
            when (val response = userRepository.authenticateWithEmailAndPassword(email, password)) {
                is Resource.Success -> {
                    loginUser(response.data)
                }
                is Resource.Error -> {
                    _loading.send(false)
                    _error.send(response.message)
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
                        _login.emit(Unit)
                    } else if (it.state == WorkInfo.State.FAILED) {
                        _loading.send(false)
                        _error.send("An error occurred")
                    }
                }
            }
        }
    }

    fun validateInputs(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            email.validateEmail().also {
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
            loginWithEmailAndPassword(email, password)
        }
    }
}