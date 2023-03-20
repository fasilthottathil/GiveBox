package com.givebox.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.common.*
import com.givebox.data.enums.MessageType
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.FragmentPrivateChatBinding
import com.givebox.domain.model.PrivateChats
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class PrivateChatFragment : BaseFragment<FragmentPrivateChatBinding>() {
    private val viewModel by viewModels<PrivateChatViewModel>()
    private val navArgs by navArgs<PrivateChatFragmentArgs>()

    @Inject
    lateinit var requestManager: RequestManager

    @Inject
    lateinit var appPreferenceManager: AppPreferenceManager
    private val privateChatAdapter by lazy {
        PrivateChatAdapter(
            requestManager,
            appPreferenceManager
        )
    }

    override fun getViewBinding(): FragmentPrivateChatBinding {
        return FragmentPrivateChatBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResults {
            it?.let {
                context.showQuestionDialog("Do you want to send this image?", "Yes","No") { canSend ->
                    if (canSend) {
                        viewModel.uploadAndSendMessage(
                            PrivateChats(messageType = MessageType.IMAGE),
                            it.data
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        navArgs.roomId?.let { roomId ->
            navArgs.roomId?.let { userId ->
                viewModel.setUserIdAndRoomId(userId, roomId)
            }
        }

        navArgs.user?.let {
            it.toObjectByGson<UserEntity>().let { userEntity ->
                binding.txtUsername.text = userEntity.name
            }
        }

        binding.rvChat.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = privateChatAdapter
        }

        binding.imgSend.setOnClickListener {
            val message = binding.edtMessage.text.toString()
            if (message.isEmpty()) return@setOnClickListener else {
                viewModel.sendMessage(
                    PrivateChats(
                        message = message.trim(),
                        messageType = MessageType.TEXT
                    )
                )
                binding.edtMessage.text?.clear()
            }
        }

        binding.imgGallery.setOnClickListener {
            launchActivityResult(Intent().apply {
                this.action = Intent.ACTION_GET_CONTENT
                this.type = "image/*"
            })
        }

        binding.imgCall.setOnClickListener {
            findNavController().navigateSafely(
                R.id.privateChatFragment,
                R.id.action_privateChatFragment_to_videoCallFragment,
                bundleOf("roomId" to navArgs.roomId.toString())
            )
        }

        binding.imgBack.setOnClickListener { findNavController().popBackStack() }

    }

    override fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.privateChats.collectLatest {
                privateChatAdapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.error.collectLatest {
                it?.let {
                    showDialog(message = it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.loading.collectLatest {
                if (it) showLoading() else hideLoading()
            }
        }

    }
}