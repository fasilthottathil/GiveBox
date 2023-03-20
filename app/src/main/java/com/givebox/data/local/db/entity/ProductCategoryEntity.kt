package com.givebox.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Fasil on 25/11/22.
 */
@Entity(tableName = "ProductCategory")
data class ProductCategoryEntity(
    @PrimaryKey
    var id: String,
    var name: String? = null,
    var image: String? = null,
    var sequence: Int? = null
)