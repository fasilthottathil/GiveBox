package com.givebox.domain.model

/**
 * Created by Fasil on 20/11/22.
 */
data class GiveBoxProduct(
    var id: String? = null,
    var name: String? = null,
    var images: String? = null,
    var categoryId: String? = null,
    var user: String? = null,
    var description: String? = null,
    var dateAdded: String? = null,
    var isActive: Boolean = true,
    var likes: String = "",
    var age: String? = null,
    var productType: String? = null
)