package com.givebox.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Fasil on 20/11/22.
 */
@Entity(tableName = "Products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var name: String? = null,
    var images: String? = null,
    var categoryId: String? = null,
    var user: String? = null,
    var description: String? = null,
    @ColumnInfo(name = "dateAdded")
    var dateAdded: String? = null,
    @ColumnInfo(name = "isActive")
    var isActive: Boolean = true,
    var likes: String = "",
    var age: String? = null,
    var productType: String? = null
)
