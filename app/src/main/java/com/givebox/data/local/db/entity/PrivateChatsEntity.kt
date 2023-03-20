package com.givebox.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.givebox.data.enums.MessageType

/**
 * Created by Fasil on 29/11/22.
 */
@Entity(tableName = "PrivateChats")
data class PrivateChatsEntity(
    @PrimaryKey
    var id: String,
    var message: String? = null,
    var messageType: MessageType? = null,
    var roomId: String? = null,
    var status: String? = null,
    var timestamp: Long? = null,
    var user: String? = null
)