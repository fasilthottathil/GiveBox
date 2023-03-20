package com.givebox.ui.chat

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.common.navigateSafely
import com.givebox.common.toGsonByObject
import com.givebox.databinding.FragmentChatBinding
import com.givebox.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>() {
    private val viewModel by viewModels<ChatViewModel>()
    @Inject lateinit var requestManager: RequestManager
    private val chatAdapter by lazy { ChatAdapter(requestManager) }
    override fun getViewBinding(): FragmentChatBinding {
        return FragmentChatBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        binding.rvChats.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter.also {
                it.setOnChatClickListener { chatsEntity ->
                    findNavController().navigateSafely(
                        R.id.chatFragment,
                        R.id.action_chatFragment_to_privateChatFragment,
                        bundleOf(
                            "roomId" to chatsEntity.roomId,
                            "userId" to chatsEntity.userId,
                            "user" to chatsEntity.user
                        )
                    )
                }
            }
        }

        binding.imgBack.setOnClickListener { activity?.onBackPressed() }
    }

    override fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.chats.collectLatest {
                chatAdapter.submitList(it)
                it?.let {
                    if (it.isNotEmpty()) binding.rvChats.smoothScrollToPosition(it.size-1)
                }
            }
        }
    }

}