package com.givebox.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.givebox.data.local.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 20/11/22.
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(usersList: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userEntity: UserEntity)

    @Delete
    fun deleteUser(userEntity: UserEntity)

    @Query("SELECT * FROM Users where id = :userId")
    fun getUserByUserID(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM Users where id = :userId")
    fun getUser(userId: String): UserEntity?

    @Query("UPDATE Users SET profileUrl = :profileUrl WHERE id = :userId")
    fun updateProfileUrl(userId: String, profileUrl: String)

}