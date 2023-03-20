package com.givebox.domain.repository

import com.givebox.common.Resource
import com.givebox.data.local.db.entity.ChatsEntity
import com.givebox.data.local.db.entity.PrivateChatsEntity
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.domain.model.PrivateChats
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fasil on 27/11/22.
 */
interface ChatRepository {
    suspend fun startChat(userEntity: UserEntity): Resource<ChatsEntity?>
    suspend fun startChatFromLocal(userEntity: UserEntity): ChatsEntity?
    suspend fun getChatsFromLocal(): Flow<List<ChatsEntity>>
    suspend fun getChatsFromServer()
    suspend fun getPrivateChatsFromServer(roomId: String)
    suspend fun getPrivateChatsFromLocal(roomId: String): Flow<List<PrivateChatsEntity>>
    suspend fun sendMessage(privateChats: PrivateChats, recieverId: String)
}