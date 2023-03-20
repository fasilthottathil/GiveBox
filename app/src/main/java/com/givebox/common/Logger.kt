package com.givebox.common

import android.util.Log

/**
 * Created by Fasil on 20/11/22.
 */
object Logger {
    fun String?.lodD(tag: String) {
        Log.d(tag, this.toString())
    }

    fun String?.logE(tag: String) {
        Log.e(tag, this.toString())
    }
}