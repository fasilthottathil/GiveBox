package com.givebox.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.givebox.common.toObjectByGson
import com.givebox.data.enums.MessageType
import com.givebox.data.local.db.entity.PrivateChatsEntity
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.ChatImageReceiveItemBinding
import com.givebox.databinding.ChatImageSendItemBinding
import com.givebox.databinding.ChatTextReceiveItemBinding
import com.givebox.databinding.ChatTextSendItemBinding

/**
 * Created by Fasil on 29/11/22.
 */
class PrivateChatAdapter constructor(
    private val requestManager: RequestManager,
    private val appPreferenceManager: AppPreferenceManager
): ListAdapter<PrivateChatsEntity, RecyclerView.ViewHolder>(PrivateChatDiff()) {

    internal class PrivateChatDiff: DiffUtil.ItemCallback<PrivateChatsEntity>() {
        override fun areItemsTheSame(
            oldItem: PrivateChatsEntity,
            newItem: PrivateChatsEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PrivateChatsEntity,
            newItem: PrivateChatsEntity
        ): Boolean {
            return false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SEND_TEXT_ITEM -> {
                TextSendViewHolder(
                    ChatTextSendItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            RECEIVE_TEXT_ITEM -> {
                TextReceiveViewHolder(
                    ChatTextReceiveItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            SEND_IMAGE_ITEM -> {
                ImageSendViewHolder(
                    ChatImageSendItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            RECEIVE_IMAGE_ITEM -> {
                ImageReceiveViewHolder(
                    ChatImageReceiveItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("Unknown viewHolder")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItem(position) == null) return
        when (holder) {
            is TextSendViewHolder -> holder.bind(getItem(position))
            is TextReceiveViewHolder -> holder.bind(getItem(position))
            is ImageSendViewHolder -> holder.bind(getItem(position))
            is ImageReceiveViewHolder -> holder.bind(getItem(position))
        }
    }

    inner class TextSendViewHolder(private val binding: ChatTextSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: PrivateChatsEntity) {
            binding.txtMessage.text = chat.message
        }
    }

    inner class TextReceiveViewHolder(private val binding: ChatTextReceiveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: PrivateChatsEntity) {
            binding.txtMessage.text = chat.message
        }
    }

    inner class ImageSendViewHolder(private val binding: ChatImageSendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: PrivateChatsEntity) {
            requestManager.load(chat.message)
                .into(binding.imgMessage)
        }
    }

    inner class ImageReceiveViewHolder(private val binding: ChatImageReceiveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: PrivateChatsEntity) {
            requestManager.load(chat.message)
                .into(binding.imgMessage)
        }
    }



    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item == null) {
            super.getItemViewType(position)
        } else {
            val user = getUser(getItem(position).user)
            when (item.messageType) {
                MessageType.TEXT -> {
                    if (appPreferenceManager.getUserId().toString() == user?.id) 0
                    else 1
                }
                MessageType.IMAGE -> {
                    if (appPreferenceManager.getUserId().toString() == user?.id) 2
                    else 3
                }
                else -> throw IllegalArgumentException("Unknown viewType")
            }
        }
    }

    private fun getUser(user: String?): UserEntity? {
        return user?.toObjectByGson<UserEntity>()
    }


    companion object {
        const val SEND_TEXT_ITEM = 0
        private const val RECEIVE_TEXT_ITEM = 1
        private const val SEND_IMAGE_ITEM = 2
        private const val RECEIVE_IMAGE_ITEM = 3
    }

}