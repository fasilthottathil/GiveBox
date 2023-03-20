package com.givebox.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.data.local.db.entity.ChatsEntity
import com.givebox.databinding.ChatItemBinding

/**
 * Created by Fasil on 27/11/22.
 */
class ChatAdapter constructor(
    private val requestManager: RequestManager
): ListAdapter<ChatsEntity, ChatAdapter.ViewHolder>(ChatsDiff()) {

    private var onChatClickListener: ((ChatsEntity) -> Unit)? = null

    class ViewHolder(private val binding: ChatItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            chatsEntity: ChatsEntity,
            requestManager: RequestManager,
            onChatClickListener: ((ChatsEntity) -> Unit)?
        ) {
            binding.txtUsername.text = chatsEntity.name
            binding.txtMessage.text = chatsEntity.message
            requestManager.load(chatsEntity.profileUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(binding.imgProfile)
            binding.root.setOnClickListener {
                onChatClickListener?.invoke(chatsEntity)
            }
        }
    }

    internal class ChatsDiff: DiffUtil.ItemCallback<ChatsEntity>() {
        override fun areItemsTheSame(oldItem: ChatsEntity, newItem: ChatsEntity): Boolean {
            return oldItem.roomId== newItem.roomId
        }

        override fun areContentsTheSame(oldItem: ChatsEntity, newItem: ChatsEntity): Boolean {
            return false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ChatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, requestManager, onChatClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setOnChatClickListener(listener: ((ChatsEntity) -> Unit)) {
        onChatClickListener  = listener
    }

}