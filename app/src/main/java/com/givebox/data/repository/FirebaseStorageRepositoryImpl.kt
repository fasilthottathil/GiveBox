package com.givebox.data.repository

import android.content.res.Resources
import android.net.Uri
import com.givebox.R
import com.givebox.domain.repository.FirebaseStorageRepository
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

/**
 * Created by Fasil on 26/11/22.
 */
class FirebaseStorageRepositoryImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val resources: Resources
) : FirebaseStorageRepository {
    override suspend fun uploadFile(
        fileUri: Uri?,
        path: String,
        onSuccess: (String) -> Unit,
        onProgress: (Double) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val storageRef = firebaseStorage.reference.child(path)
        if (fileUri == null) {
            onFailed.invoke(resources.getString(R.string.file_uri_is_null))
        } else {
            storageRef.putFile(fileUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        onSuccess.invoke(url.toString())
                    }.addOnFailureListener {
                        onFailed.invoke(it.message.toString())
                    }
                }.addOnProgressListener {
                    val progress = 100.0 * it.bytesTransferred / it.totalByteCount
                    onProgress.invoke(progress)
                }.addOnFailureListener {
                    onFailed.invoke(it.message.toString())
                }
        }
    }
}