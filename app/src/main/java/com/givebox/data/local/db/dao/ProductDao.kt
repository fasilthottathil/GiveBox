package com.givebox.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.givebox.data.local.db.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 20/11/22.
 */
@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducts(productList: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(productEntity: ProductEntity)

    @Delete
    fun deleteProduct(productEntity: ProductEntity)

    @Query("SELECT * FROM Products WHERE id = :productId")
    fun getProductById(productId: String): Flow<ProductEntity?>

    @Query("select * FROM Products WHERE isActive = 1")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @RawQuery(observedEntities = [ProductEntity::class])
    fun getProducts(query: SupportSQLiteQuery): Flow<List<ProductEntity>>

    @Query("SELECT * FROM Products ORDER BY dateAdded DESC LIMIT 1")
    fun getLastInsertedProduct(): ProductEntity?

    @Query("DELETE FROM Products WHERE id = :productId")
    fun deleteProductById(productId: String)

    @Query("UPDATE Products SET likes = :likes WHERE id = :productId")
    fun updateProductLikes(productId: String, likes: String)

}