package com.givebox.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.givebox.data.local.db.entity.ProductCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 25/11/22.
 */
@Dao
interface ProductCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProductCategory(productCategoryEntity: ProductCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProductCategories(productCategoryEntityList: List<ProductCategoryEntity>)

    @Query("SELECT * FROM ProductCategory ORDER BY sequence ASC")
    fun getProductCategories(): Flow<List<ProductCategoryEntity>>

}