package com.givebox.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.givebox.data.enums.AuthenticationType

/**
 * Created by Fasil on 20/11/22.
 */
@Entity(tableName = "Users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var password: String? = null,
    var profileUrl: String? = null,
    var isPremiumUser: Boolean = false,
    var isTerminated: Boolean = false,
    var dateJoined: String? = null,
    var authenticationType: AuthenticationType
)
