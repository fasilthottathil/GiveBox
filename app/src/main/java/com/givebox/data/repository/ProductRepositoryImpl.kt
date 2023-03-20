package com.givebox.data.repository

import android.content.res.Resources
import androidx.sqlite.db.SupportSQLiteQuery
import com.givebox.R
import com.givebox.common.Constants
import com.givebox.common.Logger.logE
import com.givebox.common.Resource
import com.givebox.common.mapObject
import com.givebox.data.local.db.AppDatabase
import com.givebox.data.local.db.entity.ProductEntity
import com.givebox.domain.model.GiveBoxProduct
import com.givebox.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Created by Fasil on 24/11/22.
 */
class ProductRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val resources: Resources,
    private val firebaseFirestore: FirebaseFirestore
) : ProductRepository {
    override suspend fun insertProduct(giveBoxProduct: GiveBoxProduct): Resource<ProductEntity?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                .add(giveBoxProduct)
                .await()
        }.onSuccess {
            giveBoxProduct.mapObject<GiveBoxProduct, ProductEntity>()?.let {
                appDatabase.productDao().insertProduct(it)
                return Resource.Success(it)
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun getProducts(query: SupportSQLiteQuery): Flow<List<ProductEntity>> {
        return appDatabase.productDao().getProducts(query)
    }

    override suspend fun getAllProducts(): Flow<List<ProductEntity>> {
        return appDatabase.productDao().getAllProducts()
    }


    override suspend fun getProductsFromServer(): Resource<List<ProductEntity>?> {
        kotlin.runCatching {
            val lastInsertedProduct = appDatabase.productDao().getLastInsertedProduct()
            if (lastInsertedProduct == null) {
                return@runCatching firebaseFirestore.collection(Constants.PRODUCTS).limit(2000)
                    .get()
                    .await()
            } else {
                return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                    .startAfter(Constants.ID, lastInsertedProduct.id)
                    .limit(2000)
                    .get()
                    .await()
            }
        }.onSuccess {
            it?.let {
                val productEntityList = mutableListOf<ProductEntity>()
                it.toObjects(GiveBoxProduct::class.java).forEach { giveBoxProduct ->
                    giveBoxProduct.mapObject<GiveBoxProduct, ProductEntity>()?.let { productEntity ->
                        productEntityList.add(productEntity)
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    appDatabase.productDao().insertProducts(productEntityList)
                }
                return Resource.Success(productEntityList)
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun deleteProductFromServer(productId: String): Resource<Unit?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                .whereEqualTo(Constants.ID, productId)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            it?.let {
                kotlin.runCatching {
                    return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                        .document(it.documents[0].id)
                        .delete()
                        .await()
                }.onSuccess {
                    deleteProductFromLocal(productId)
                    return Resource.Success(Unit)
                }.onFailure { error ->
                    error.message?.let { message ->
                        message.logE(TAG)
                    }
                }
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun deleteProductFromLocal(productId: String) {
        appDatabase.productDao().deleteProductById(productId)
    }

    override suspend fun likeProduct(productId: String, userId: String) {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                .whereEqualTo(Constants.ID, productId)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            it?.let {
                val likes = it.toObjects(GiveBoxProduct::class.java)[0].likes
                val likeMap = (if (likes.isEmpty()) {
                    mutableMapOf(userId to true)
                } else {
                    likes.replace("[{}]".toRegex(),"").split(",").associate { str ->
                        val (left, right) = str.split("=")
                        left to right
                    }
                }).toMutableMap()
                likeMap[userId] = true
                kotlin.runCatching {
                    return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                        .document(it.documents[0].id)
                        .update(mapOf("likes" to likeMap.toString()))
                        .await()
                }.onSuccess {
                    appDatabase.productDao().updateProductLikes(productId, likeMap.toString())
                }
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
    }

    override suspend fun unLikeProduct(productId: String, userId: String) {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                .whereEqualTo(Constants.ID, productId)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            it?.let {
                val likes = it.toObjects(GiveBoxProduct::class.java)[0].likes
                val likeMap = (if (likes.isEmpty()) {
                    mutableMapOf(userId to false)
                } else {
                    likes.replace("[{}]".toRegex(),"").split(",").associate { str ->
                        val (left, right) = str.split("=")
                        left to right
                    }
                }).toMutableMap()
                likeMap[userId] = false
                kotlin.runCatching {
                    return@runCatching firebaseFirestore.collection(Constants.PRODUCTS)
                        .document(it.documents[0].id)
                        .update(mapOf("likes" to likeMap.toString()))
                        .await()
                }.onSuccess {
                    appDatabase.productDao().updateProductLikes(productId, likeMap.toString())
                }
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
    }

    override suspend fun getProductById(productId: String):  Flow<ProductEntity?> {
        return appDatabase.productDao().getProductById(productId)
    }

    companion object {
        private const val TAG = "ProductRepository"
    }

}