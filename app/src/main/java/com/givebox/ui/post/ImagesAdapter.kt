package com.givebox.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.databinding.ImageItemBinding

/**
 * Created by Fasil on 27/11/22.
 */
class ImagesAdapter constructor(
    private val requestManager: RequestManager
): ListAdapter<String, ImagesAdapter.ViewHolder>(ImagesDiff()) {
    class ViewHolder(private val binding: ImageItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String, requestManager: RequestManager) {
            requestManager.load(imageUrl.trim())
                .error(R.drawable.ic_product)
                .placeholder(R.drawable.ic_product)
                .into(binding.imgProduct)
        }
    }

    internal class ImagesDiff: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ImageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, requestManager)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

}