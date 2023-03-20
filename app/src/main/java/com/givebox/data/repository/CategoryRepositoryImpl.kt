package com.givebox.data.repository

import android.content.res.Resources
import com.givebox.R
import com.givebox.common.Constants
import com.givebox.common.Logger.logE
import com.givebox.common.Resource
import com.givebox.common.mapObject
import com.givebox.data.local.db.AppDatabase
import com.givebox.data.local.db.entity.ProductCategoryEntity
import com.givebox.domain.model.GiveBoxProductCategory
import com.givebox.domain.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Created by Fasil on 25/11/22.
 */
class CategoryRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val resources: Resources,
    private val firebaseFirestore: FirebaseFirestore
) : CategoryRepository {

    override suspend fun getAllCategoriesFromServer(): Resource<List<ProductCategoryEntity>?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.PRODUCT_CATEGORIES)
                .get()
                .await()
        }.onSuccess {
            it?.let {
                val productCategoryEntityList = mutableListOf<ProductCategoryEntity>()
                it.toObjects(GiveBoxProductCategory::class.java).forEach { category ->
                    category.mapObject<GiveBoxProductCategory, ProductCategoryEntity>()?.let { entity ->
                        productCategoryEntityList.add(entity)
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    appDatabase.productCategoryDao().insertProductCategories(productCategoryEntityList)
                }
                return Resource.Success(productCategoryEntityList)
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun getAllProductCategoriesFromLocal(): Flow<List<ProductCategoryEntity>> {
        return appDatabase.productCategoryDao().getProductCategories()
    }

    companion object {
        private const val TAG = "CategoryRepository"
    }

}