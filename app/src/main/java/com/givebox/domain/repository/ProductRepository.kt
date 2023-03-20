package com.givebox.domain.repository

import androidx.sqlite.db.SupportSQLiteQuery
import com.givebox.common.Resource
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.domain.model.GiveBoxProduct
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 20/11/22.
 */
interface ProductRepository {
    suspend fun insertProduct(giveBoxProduct: GiveBoxProduct): Resource<ProductEntity?>
    suspend fun getProducts(query: SupportSQLiteQuery): Flow<List<ProductEntity>>
    suspend fun getAllProducts(): Flow<List<ProductEntity>>
    suspend fun getProductsFromServer(): Resource<List<ProductEntity>?>
    suspend fun deleteProductFromServer(productId: String): Resource<Unit?>
    suspend fun deleteProductFromLocal(productId: String)
    suspend fun likeProduct(productId: String, userId: String)
    suspend fun unLikeProduct(productId: String, userId: String)
    suspend fun getProductById(productId: String): Flow<ProductEntity?>
}