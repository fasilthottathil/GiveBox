package com.givebox.data.local.pref

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import javax.inject.Inject

/**
 * Created by Fasil on 20/11/22.
 */
class AppPreferenceManager @Inject constructor(application: Application) {
    companion object {
        private const val PREF_NAME = "GIVE_BOX_PREFERENCE"
        private const val PREF_AUTHENTICATED = "AUTHENTICATED"
        private const val PREF_USER_ID = "PREF_USER_ID"
    }

    private val sharedPreferences = application.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    fun isAuthenticated() = sharedPreferences.getBoolean(PREF_AUTHENTICATED, false)

    fun setAuthenticated(authenticated: Boolean, userId: String?) = sharedPreferences.edit {
        putBoolean(PREF_AUTHENTICATED, authenticated)
        putString(PREF_USER_ID, userId)
        apply()
    }

    fun getUserId() = sharedPreferences.getString(PREF_USER_ID, null)

    fun logout() = sharedPreferences.edit {
        putBoolean(PREF_AUTHENTICATED, false)
        putString(PREF_USER_ID, null)
        apply()
        commit()
    }
}