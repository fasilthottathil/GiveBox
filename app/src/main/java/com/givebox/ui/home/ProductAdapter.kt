package com.givebox.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.common.toObjectByGson
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.ProductItemBinding

/**
 * Created by Fasil on 26/11/22.
 */
class ProductAdapter constructor(
    private val requestManager: RequestManager,
    private val appPreferenceManager: AppPreferenceManager
) : ListAdapter<ProductEntity, ProductAdapter.ViewHolder>(ProductDiff()) {

    private var onProductClickListener: ((ProductEntity) -> Unit)? = null
    private var onLikeClickListener: ((ProductEntity, Boolean) -> Unit)? = null

    class ViewHolder(private val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            productEntity: ProductEntity,
            requestManager: RequestManager,
            onProductClickListener: ((ProductEntity) -> Unit)?,
            onLikeClickListener: ((ProductEntity, Boolean) -> Unit)?,
            appPreferenceManager: AppPreferenceManager,
        ) {
            requestManager.load(productEntity.images.toString().replace("[\\[\\]]".toRegex(),"").split(",")[0])
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(binding.imgProduct)
            binding.txtProduct.text = productEntity.name
            binding.txtProductType.text = productEntity.productType
            binding.txtProductDesc.text = productEntity.description
             productEntity.user?.toObjectByGson<UserEntity>()?.let {
                binding.txtUsername.text = it.name
            }
            var isLiked = false
            if (productEntity.likes.isNotEmpty()) {
                val likeMap = productEntity.likes.replace("[{}]".toRegex(),"").split(",").associate { str ->
                    val (left, right) = str.split("=")
                    left to right
                }
                if (likeMap.containsKey(appPreferenceManager.getUserId())) {
                    if (likeMap[appPreferenceManager.getUserId().toString()] == "true") {
                        isLiked = true
                        binding.imgFavourite.imageTintList = ColorStateList.valueOf(Color.RED)
                    } else {
                        isLiked = false
                        binding.imgFavourite.imageTintList = ColorStateList.valueOf(Color.GRAY)
                    }
                } else {
                    binding.imgFavourite.imageTintList = ColorStateList.valueOf(Color.GRAY)
                }
            }

            binding.imgFavourite.setOnClickListener {
                if (isLiked) {
                    binding.imgFavourite.imageTintList = ColorStateList.valueOf(Color.GRAY)
                } else {
                    binding.imgFavourite.imageTintList = ColorStateList.valueOf(Color.RED)
                }
                onLikeClickListener?.invoke(productEntity, !isLiked)
            }

            binding.root.setOnClickListener { onProductClickListener?.invoke(productEntity) }
        }
    }

    internal class ProductDiff : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(
                it,
                requestManager,
                onProductClickListener,
                onLikeClickListener,
                appPreferenceManager
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setOnProductClickListener(listener: ((ProductEntity) -> Unit)) {
        onProductClickListener = listener
    }

    fun setOnLikeClickListener(listener: ((ProductEntity, Boolean) -> Unit)) {
        onLikeClickListener = listener
    }

}