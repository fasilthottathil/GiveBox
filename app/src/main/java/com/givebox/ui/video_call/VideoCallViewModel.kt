package com.givebox.ui.video_call

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.givebox.data.local.pref.AppPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.rtc2.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random


/**
 * Created by Fasil on 06/12/22.
 */
@HiltViewModel
class VideoCallViewModel @Inject constructor(private val application: Application) : ViewModel() {

    private val _error: Channel<String?> = Channel()
    val error get() = _error.receiveAsFlow()
    private val _localVideo: MutableStateFlow<Pair<Int, RtcEngine?>?> = MutableStateFlow(null)
    val localVideo get() = _localVideo.asStateFlow()
    private val _remoteVideo: MutableStateFlow<Pair<Int, RtcEngine?>?> = MutableStateFlow(null)
    val remoteVideo get() = _remoteVideo.asStateFlow()
    private val appId = "669460fcbc704854bd64e75033db4f46"
    private var channelName = ""
    private val token = "<your access token>"
    private val uid = (1..Int.MAX_VALUE).random()
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null

    private fun setupVideoSDKEngine() {
        viewModelScope.launch {
            try {
                val config = RtcEngineConfig()
                config.mContext = application
                config.mAppId = appId
                config.mEventHandler = mRtcEventHandler
                agoraEngine = RtcEngine.create(config)
                agoraEngine?.enableVideo()
                val options = ChannelMediaOptions()
                options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                _localVideo.emit(Pair(uid, agoraEngine))
                agoraEngine?.startPreview()
                agoraEngine?.joinChannel(null, channelName, uid, options)
            } catch (e: Exception) {
                _error.send(e.message)
            }
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            viewModelScope.launch {
                _remoteVideo.emit(Pair(uid, agoraEngine))
            }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            viewModelScope.launch {
                _error.send("User is offline")
            }
        }
    }

    fun startVideoCall(roomId: String) {
        viewModelScope.launch {
            channelName = roomId
            setupVideoSDKEngine()
        }
    }

    override fun onCleared() {
        super.onCleared()
        agoraEngine?.stopPreview()
        agoraEngine?.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

}