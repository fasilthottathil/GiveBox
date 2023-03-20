package com.givebox.domain.model

import com.givebox.data.enums.AuthenticationType

/**
 * Created by Fasil on 20/11/22.
 */
data class GiveBoxUser(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var password: String? = null,
    var profileUrl: String? = null,
    var isPremiumUser: Boolean = false,
    var isTerminated: Boolean = false,
    var dateJoined: String? = null,
    var authenticationType: AuthenticationType? = null
)
