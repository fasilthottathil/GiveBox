package com.givebox.domain.repository

import android.net.Uri

/**
 * Created by Fasil on 26/11/22.
 */
interface FirebaseStorageRepository {
    suspend fun uploadFile(
        fileUri: Uri?,
        path: String = "uploads/${System.currentTimeMillis()}",
        onSuccess: (String) -> Unit,
        onProgress: (Double) -> Unit,
        onFailed: (String) -> Unit
    )
}