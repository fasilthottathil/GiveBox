package com.givebox.domain.repository

import com.givebox.common.Resource
import com.givebox.data.local.db.entity.ProductCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 25/11/22.
 */
interface CategoryRepository {
    suspend fun getAllCategoriesFromServer(): Resource<List<ProductCategoryEntity>?>
    suspend fun getAllProductCategoriesFromLocal(): Flow<List<ProductCategoryEntity>>
}