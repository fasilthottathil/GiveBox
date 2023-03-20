package com.givebox.domain.repository

import com.givebox.common.Resource
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.domain.model.GiveBoxUser
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 20/11/22.
 */
interface UserRepository {
    suspend fun registerUser(giveBoxUser: GiveBoxUser): Resource<UserEntity?>
    suspend fun login(id: String?): Resource<UserEntity?>
    suspend fun authenticateWithGoogle(idToken: String): Resource<String?>
    suspend fun registerWithGoogle(idToken: String): Resource<String?>
    suspend fun registerWithEmailAndPassword(email: String, password: String): Resource<String?>
    suspend fun authenticateWithEmailAndPassword(email: String, password: String): Resource<String?>
    suspend fun getUserById(userId: String): Flow<UserEntity?>
    suspend fun getUser(userId: String): UserEntity?
    suspend fun updateProfilePicture(userId: String, imageUrl: String): Resource<Unit?>
    suspend fun getPasswordByPhoneNumber(phoneNumber: String): Resource<String?>
    suspend fun updateUser(giveBoxUser: GiveBoxUser): Resource<UserEntity?>

}