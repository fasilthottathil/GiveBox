package com.givebox.data.repository

import android.content.res.Resources
import com.givebox.R
import com.givebox.common.Constants
import com.givebox.common.Logger.logE
import com.givebox.common.Resource
import com.givebox.common.mapObject
import com.givebox.data.local.db.AppDatabase
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.domain.model.GiveBoxUser
import com.givebox.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Created by Fasil on 20/11/22.
 */
class UserRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val resources: Resources,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val appPreferenceManager: AppPreferenceManager
) : UserRepository {
    override suspend fun registerUser(giveBoxUser: GiveBoxUser): Resource<UserEntity?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.USERS)
                .add(giveBoxUser)
                .await()
        }.onSuccess {
            it?.let {
                giveBoxUser.mapObject<GiveBoxUser, UserEntity>()?.let { userEntity ->
                    appDatabase.userDao().insertUser(userEntity)
                    appPreferenceManager.setAuthenticated(true, userEntity.id)
                    return Resource.Success(userEntity)
                }
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
                return Resource.Error(message)
            }
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun login(id: String?): Resource<UserEntity?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.ID, id)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            it?.let {
                if (it.isEmpty) {
                    resources.getString(R.string.invalid_credentials).also { message ->
                        message.logE(TAG)
                        return Resource.Error(message)
                    }
                } else {
                    it.documents[0].toObject(GiveBoxUser::class.java)?.let { giveBoxUser ->
                        giveBoxUser.mapObject<GiveBoxUser, UserEntity>()?.let { userEntity ->
                            appPreferenceManager.setAuthenticated(true, userEntity.id)
                            appDatabase.userDao().insertUser(userEntity)
                            return Resource.Success(userEntity)
                        }
                    }
                }
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
                return Resource.Error(message)
            }
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun authenticateWithGoogle(idToken: String): Resource<String?> {
        kotlin.runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            return@runCatching firebaseAuth.signInWithCredential(credential).await()
        }.onSuccess {
            return if (it.user == null) {
                Resource.Error(resources.getString(R.string.user_not_found))
            } else {
                Resource.Success(it.user?.uid)
            }
        }.onFailure {
            return Resource.Error(it.message)
        }

        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun registerWithGoogle(idToken: String): Resource<String?> {
        kotlin.runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            return@runCatching firebaseAuth.signInWithCredential(credential).await()
        }.onSuccess {
            return if (it.user == null) {
                Resource.Error(resources.getString(R.string.something_went_wrong))
            } else {
                Resource.Success(it.user?.uid)
            }
        }.onFailure {
            return Resource.Error(it.message)
        }

        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun registerWithEmailAndPassword(
        email: String,
        password: String
    ): Resource<String?> {
        kotlin.runCatching {
            return@runCatching firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        }.onSuccess {
            return if (it.user == null) {
                Resource.Error(resources.getString(R.string.something_went_wrong))
            } else {
                kotlin.runCatching {
                    return@runCatching firebaseFirestore.collection(Constants.USERS)
                        .whereEqualTo(Constants.ID, it.user?.uid)
                        .limit(1)
                        .get()
                        .await()
                }.onSuccess { response ->
                    return if (response.isEmpty) {
                        Resource.Success(it.user?.uid)
                    } else {
                        Resource.Error(resources.getString(R.string.user_exist))
                    }
                }.onFailure { e ->
                    return Resource.Error(e.message)
                }
                Resource.Error(resources.getString(R.string.something_went_wrong))
            }
        }.onFailure {
            return Resource.Error(it.message)
        }

        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun authenticateWithEmailAndPassword(
        email: String,
        password: String
    ): Resource<String?> {
        kotlin.runCatching {
            return@runCatching firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }.onSuccess {
            return if (it.user == null) {
                Resource.Error(resources.getString(R.string.invalid_credentials))
            } else {
                Resource.Success(it.user?.uid)
            }
        }.onFailure {
            return Resource.Error(it.message)
        }

        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun getUserById(userId: String): Flow<UserEntity?> {
        return appDatabase.userDao().getUserByUserID(userId)
    }

    override suspend fun getUser(userId: String): UserEntity? {
        return appDatabase.userDao().getUser(userId)
    }

    override suspend fun updateProfilePicture(userId: String, imageUrl: String): Resource<Unit?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.ID, userId)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            it?.let {
                kotlin.runCatching {
                    return@runCatching firebaseFirestore.collection(Constants.USERS)
                        .document(it.documents[0].id)
                        .update(mapOf("profileUrl" to imageUrl))
                        .await()
                }.onSuccess {
                    appDatabase.userDao().updateProfileUrl(userId, imageUrl)
                    return Resource.Success(Unit)
                }.onFailure { error ->
                    error.message?.let { message ->
                        message.logE(TAG)
                        return Resource.Error(message)
                    }
                }
            }
        }.onFailure {
            return Resource.Error(it.message)
        }

        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun getPasswordByPhoneNumber(phoneNumber: String): Resource<String?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo("phone", phoneNumber)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            return if (it.isEmpty) {
                Resource.Error(resources.getString(R.string.forgot_pass_user_not_found_err))
            } else {
                Resource.Success(it.toObjects(GiveBoxUser::class.java)[0].password)
            }
        }.onFailure {
            return Resource.Error(it.message)
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun updateUser(giveBoxUser: GiveBoxUser): Resource<UserEntity?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.USERS)
                .whereEqualTo(Constants.ID, giveBoxUser.id)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            if (it.isEmpty) {
                return Resource.Error(resources.getString(R.string.user_not_found))
            } else {
                kotlin.runCatching {
                    return@runCatching firebaseFirestore.collection(Constants.USERS)
                        .document(it.documents[0].id)
                        .update(
                            mapOf(
                                "name" to giveBoxUser.name,
                                "phone" to giveBoxUser.phone,
                                "password" to giveBoxUser.password,
                                "profileUrl" to giveBoxUser.profileUrl,
                                "email" to giveBoxUser.email
                            )
                        )
                        .await()
                }.onSuccess {
                    giveBoxUser.mapObject<GiveBoxUser, UserEntity>()?.let { userEntity ->
                        appDatabase.userDao().insertUser(userEntity)
                        return Resource.Success(userEntity)
                    }
                }.onFailure { throwable ->
                    throwable.message?.let { message ->
                        message.logE(TAG)
                        return Resource.Error(message)
                    }
                }
            }
        }.onFailure {
            return Resource.Error(it.message)
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    companion object {
        private const val TAG = "UserRepository"
    }

}