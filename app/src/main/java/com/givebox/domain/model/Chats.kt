package com.givebox.domain.model

/**
 * Created by Fasil on 27/11/22.
 */
data class Chats(
    var roomId: String? = null,
    var name: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var message: String? = null,
    var profileUrl: String? = null,
    var user: String? = null
)
