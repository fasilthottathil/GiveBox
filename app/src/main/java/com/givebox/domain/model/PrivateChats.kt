package com.givebox.domain.model

import com.givebox.data.enums.MessageType


/**
 * Created by Fasil on 29/11/22.
 */
data class PrivateChats(
    var id: String? = null,
    var message: String? = null,
    var messageType: MessageType? = null,
    var roomId: String? = null,
    var status: String? = null,
    var timestamp: Long? = null,
    var user: String? = null
)