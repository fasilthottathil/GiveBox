package com.givebox.common

/**
 *Created by Fasil on 7/16/2022
 */
sealed class Resource<T>(val data: T?, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String?, data: T? = null) : Resource<T>(data, message)
}
