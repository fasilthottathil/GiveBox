package com.givebox.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.givebox.data.local.db.entity.ChatsEntity
import com.givebox.data.local.db.entity.PrivateChatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 27/11/22.
 */
@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChats(chatsEntityList: List<ChatsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chatsEntity: ChatsEntity)

    @Query("SELECT * FROM Chats WHERE userId = :userId")
    fun getChatByUserId(userId: String): ChatsEntity?

    @Query("SELECT * FROM Chats ORDER BY timestamp ASC")
    fun getAllChats(): Flow<List<ChatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPrivateChat(privateChatsEntity: PrivateChatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPrivateChats(privateChatsEntityList: List<PrivateChatsEntity>)

    @Query("SELECT * FROM PrivateChats WHERE roomId =:roomId AND status = 'Active' ORDER BY timestamp ASC")
    fun getPrivateChats(roomId: String): Flow<List<PrivateChatsEntity>>



}