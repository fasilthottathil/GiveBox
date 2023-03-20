package com.givebox.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.databinding.FavouriteItemBinding
import com.givebox.databinding.MyProductItemBinding

/**
 * Created by Fasil on 27/11/22.
 */
class MyProductsAdapter constructor(
    private val requestManager: RequestManager
): ListAdapter<ProductEntity, MyProductsAdapter.ViewHolder>(FavouriteDiff()) {

    private var onProductDeleteClickListener: ((ProductEntity) -> Unit)? = null

    class ViewHolder(private val binding: MyProductItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            productEntity: ProductEntity,
            requestManager: RequestManager,
            onDislikeClickListener: ((ProductEntity) -> Unit)?
        ) {
            requestManager.load(productEntity.images.toString().replace("[\\[\\]]".toRegex(),"").split(",")[0])
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(binding.imgProduct)
            binding.txtProduct.text = productEntity.name
            binding.imgDelete.setOnClickListener {
                onDislikeClickListener?.invoke(productEntity)
            }
        }
    }

    internal class FavouriteDiff: DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MyProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, requestManager, onProductDeleteClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setOnProductDeleteClickListener(listener: ((ProductEntity)->Unit)) {
        onProductDeleteClickListener = listener
    }

}