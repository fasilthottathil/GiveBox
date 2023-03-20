package com.givebox.ui.video_call

import android.Manifest
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.givebox.common.checkPermissions
import com.givebox.databinding.FragmentVideoCallBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class VideoCallFragment : BaseFragment<FragmentVideoCallBinding>() {
    private val viewModel by viewModels<VideoCallViewModel>()
    private val navArgs by navArgs<VideoCallFragmentArgs>()
    override fun getViewBinding(): FragmentVideoCallBinding {
        return FragmentVideoCallBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context.checkPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        ) {
            if (it) {
                navArgs.roomId?.let { roomId -> viewModel.startVideoCall(roomId) }
            } else {
                activity?.let { fragmentActivity ->
                    ActivityCompat.requestPermissions(
                        fragmentActivity,
                        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                        1
                    )
                }
                Toast.makeText(context, "Permission required", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }

        initViews()
        observe()
    }

    override fun initViews() {
        binding.imgEndCall.setOnClickListener { findNavController().popBackStack() }
    }

    override fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.error.collectLatest {
                it?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.localVideo.collectLatest {
                it?.let {
                    val localSurfaceView = SurfaceView(context)
                    binding.localVideoViewContainer.addView(localSurfaceView)
                    it.second?.setupLocalVideo(
                        VideoCanvas(
                            localSurfaceView,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            it.first
                        )
                    )
                    localSurfaceView.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.remoteVideo.collectLatest {
                it?.let {
                    val remoteSurfaceView = SurfaceView(context)
                    remoteSurfaceView.setZOrderMediaOverlay(true)
                    binding.remoteVideoViewContainer.addView(remoteSurfaceView)
                    it.second?.setupLocalVideo(
                        VideoCanvas(
                            remoteSurfaceView,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            it.first
                        )
                    )
                    remoteSurfaceView.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        context.checkPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        ) {
            if (!it) {
                Toast.makeText(context, "Permission required", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }
    }

}