package com.givebox.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.data.local.db.entity.ProductCategoryEntity
import com.givebox.databinding.CategoryItemBinding

/**
 * Created by Fasil on 26/11/22.
 */
class ProductCategoryAdapter constructor(
    private val requestManager: RequestManager,
    private val application: Application
): ListAdapter<ProductCategoryEntity, ProductCategoryAdapter.ViewHolder>(CategoryDiff()) {

    private var selectedCategoryId:String? = null
    private var onSelectCategory: ((ProductCategoryEntity?) -> Unit)? = null

    class ViewHolder(private val binding: CategoryItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(
            productCategoryEntity: ProductCategoryEntity,
            adapter: ProductCategoryAdapter
        ) {
            binding.txtCategory.setTextColor(Color.WHITE)
            binding.root.setCardBackgroundColor(ContextCompat.getColor(adapter.application, R.color.secondary))
            binding.imgCategory.imageTintList = ColorStateList.valueOf(Color.WHITE)
            if (adapter.selectedCategoryId != null) {
                if (adapter.selectedCategoryId == productCategoryEntity.id) {
                    binding.imgCategory.imageTintList = ColorStateList.valueOf(Color.BLACK)
                    binding.txtCategory.setTextColor(Color.BLACK)
                    binding.root.setCardBackgroundColor(Color.WHITE)
                }
            }
            binding.txtCategory.text = productCategoryEntity.name
            adapter.requestManager.load(productCategoryEntity.image)
                .error(R.drawable.ic_product)
                .into(binding.imgCategory)
            binding.root.setOnClickListener {
                if (adapter.selectedCategoryId == productCategoryEntity.id) {
                    adapter.selectedCategoryId = null
                    adapter.onSelectCategory?.invoke(null)
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.selectedCategoryId = productCategoryEntity.id
                    adapter.onSelectCategory?.invoke(productCategoryEntity)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    internal class CategoryDiff: DiffUtil.ItemCallback<ProductCategoryEntity>() {
        override fun areItemsTheSame(
            oldItem: ProductCategoryEntity,
            newItem: ProductCategoryEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ProductCategoryEntity,
            newItem: ProductCategoryEntity
        ): Boolean {
            return false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it,this)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return (position)
    }

    fun setOnProductCategorySelectListener(listener: ((ProductCategoryEntity?) -> Unit)) {
        onSelectCategory = listener
    }

}