package com.givebox.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Fasil on 27/11/22.
 */
@Entity(tableName = "Chats")
data class ChatsEntity(
    @PrimaryKey
    var roomId: String,
    var name: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var message: String? = null,
    var profileUrl: String? = null,
    var user: String? = null
)