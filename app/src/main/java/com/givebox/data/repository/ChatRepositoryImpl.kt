package com.givebox.data.repository

import android.content.res.Resources
import com.givebox.R
import com.givebox.common.Constants
import com.givebox.common.Logger.logE
import com.givebox.common.Resource
import com.givebox.common.mapObject
import com.givebox.common.toGsonByObject
import com.givebox.data.local.db.AppDatabase
import com.givebox.data.local.db.entity.ChatsEntity
import com.givebox.data.local.db.entity.PrivateChatsEntity
import com.givebox.data.local.db.entity.UserEntity
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.domain.model.Chats
import com.givebox.domain.model.PrivateChats
import com.givebox.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * Created by Fasil on 27/11/22.
 */
class ChatRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val resources: Resources,
    private val firebaseFirestore: FirebaseFirestore,
    private val userRepository: UserRepositoryImpl,
    private val appPreferenceManager: AppPreferenceManager
): ChatRepository {
    override suspend fun startChat(userEntity: UserEntity): Resource<ChatsEntity?> {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.CHATS)
                .document(appPreferenceManager.getUserId().toString())
                .collection(Constants.CHATS)
                .whereEqualTo("userId", userEntity.id)
                .limit(1)
                .get()
                .await()
        }.onSuccess {
            if (it.isEmpty) {
                val roomId = UUID.randomUUID().toString()
                kotlin.runCatching {
                    return@runCatching firebaseFirestore.collection(Constants.CHATS)
                        .document(appPreferenceManager.getUserId().toString())
                        .collection(Constants.CHATS)
                        .add(
                            Chats(
                                roomId,
                                userEntity.name,
                                userEntity.id,
                                System.currentTimeMillis(),
                                "",
                                userEntity.profileUrl,
                                userEntity.toGsonByObject()
                            )
                        )
                        .await()
                }.onSuccess {
                    kotlin.runCatching {
                        return@runCatching firebaseFirestore.collection(Constants.CHATS)
                            .document(userEntity.id)
                            .collection(Constants.CHATS)
                            .add(
                                Chats(
                                    roomId,
                                    userRepository.getUser(
                                        appPreferenceManager.getUserId().toString()
                                    )?.name,
                                    appPreferenceManager.getUserId().toString(),
                                    System.currentTimeMillis(),
                                    "",
                                    userRepository.getUser(
                                        appPreferenceManager.getUserId().toString()
                                    )?.profileUrl,
                                    userRepository.getUser(
                                        appPreferenceManager.getUserId().toString()
                                    ).toGsonByObject()
                                )
                            )
                            .await()
                    }.onSuccess {
                        val chatsEntity = ChatsEntity(roomId, userEntity.name, userEntity.id, System.currentTimeMillis(),"")
                        appDatabase.chatDao().insertChat(chatsEntity)
                        return Resource.Success(chatsEntity)

                    }.onFailure { throwable ->
                        throwable.message?.let { message ->
                            message.logE(TAG)
                            return Resource.Error(message)
                        }
                    }
                }.onFailure { throwable ->
                    throwable.message?.let { message ->
                        message.logE(TAG)
                        return Resource.Error(message)
                    }
                }
            } else {
                val chatEntityList = mutableListOf<ChatsEntity>()
                it.toObjects(Chats::class.java).forEach { chats ->
                    chats.mapObject<Chats,ChatsEntity>()?.let { chatsEntity ->
                        chatEntityList.add(chatsEntity)
                    }
                }
                appDatabase.chatDao().insertChats(chatEntityList)
                return if (chatEntityList.isNotEmpty())
                    Resource.Success(chatEntityList[0])
                else
                    Resource.Error(resources.getString(R.string.cannot_start_chat))
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
                return Resource.Error(message)
            }
        }
        return Resource.Error(resources.getString(R.string.something_went_wrong))
    }

    override suspend fun startChatFromLocal(userEntity: UserEntity): ChatsEntity? {
        return appDatabase.chatDao().getChatByUserId(userEntity.id)
    }

    override suspend fun getChatsFromLocal(): Flow<List<ChatsEntity>> {
        return appDatabase.chatDao().getAllChats()
    }

    override suspend fun getChatsFromServer() {
        kotlin.runCatching {
            firebaseFirestore.collection(Constants.CHATS)
                .document(appPreferenceManager.getUserId().toString())
                .collection(Constants.CHATS)
                .addSnapshotListener { value, error ->
                    if (error != null) return@addSnapshotListener
                    else {
                        value?.let {
                            val chatEntityList = mutableListOf<ChatsEntity>()
                            value.toObjects(Chats::class.java).forEach {
                                it.mapObject<Chats,ChatsEntity>()?.let { chatsEntity ->
                                    chatEntityList.add(chatsEntity)
                                }
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                appDatabase.chatDao().insertChats(chatEntityList)
                            }
                        }
                    }
                }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
    }

    override suspend fun getPrivateChatsFromServer(roomId: String) {
        kotlin.runCatching {
            firebaseFirestore.collection(Constants.PRIVATE_CHATS)
                .document(roomId)
                .collection(Constants.PRIVATE_CHATS)
                .limit(150)
                .addSnapshotListener { value, error ->
                    if (error != null) return@addSnapshotListener
                    value?.let {
                        val privateChatsEntityList = mutableListOf<PrivateChatsEntity>()
                        it.toObjects(PrivateChats::class.java).forEach { privateChats ->
                            privateChats.mapObject<PrivateChats,PrivateChatsEntity>()?.let { entity ->
                                privateChatsEntityList.add(entity)
                            }
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            appDatabase.chatDao().insertPrivateChats(privateChatsEntityList)
                        }
                    }
                }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
    }

    override suspend fun getPrivateChatsFromLocal(roomId: String): Flow<List<PrivateChatsEntity>> {
        return appDatabase.chatDao().getPrivateChats(roomId)
    }

    override suspend fun sendMessage(privateChats: PrivateChats, recieverId: String) {
        kotlin.runCatching {
            return@runCatching firebaseFirestore.collection(Constants.PRIVATE_CHATS)
                .document(privateChats.roomId.toString())
                .collection(Constants.PRIVATE_CHATS)
                .add(privateChats)
                .await()
        }.onSuccess {
            kotlin.runCatching {
                return@runCatching firebaseFirestore.collection(Constants.CHATS)
                    .document(recieverId)
                    .collection(Constants.CHATS)
                    .whereEqualTo("roomId", privateChats.roomId)
                    .limit(1)
                    .get()
                    .await()
            }.onSuccess {
                it?.let {
                    kotlin.runCatching {
                        if (it.documents.isEmpty()) return
                        return@runCatching firebaseFirestore.collection(Constants.CHATS)
                            .document(recieverId)
                            .collection(Constants.CHATS)
                            .document(it.documents[0].id)
                            .update(mapOf("message" to privateChats.message, "timestamp" to System.currentTimeMillis()))
                            .await()
                    }.onSuccess {
                        kotlin.runCatching {
                            return@runCatching firebaseFirestore.collection(Constants.CHATS)
                                .document(appPreferenceManager.getUserId().toString())
                                .collection(Constants.CHATS)
                                .whereEqualTo("roomId", privateChats.roomId)
                                .limit(1)
                                .get()
                                .await()
                        }.onSuccess { snapshot ->
                            snapshot?.let {
                                kotlin.runCatching {
                                    if (snapshot.documents.isEmpty()) return
                                    return@runCatching firebaseFirestore.collection(Constants.CHATS)
                                        .document(appPreferenceManager.getUserId().toString())
                                        .collection(Constants.CHATS)
                                        .document(snapshot.documents[0].id)
                                        .update(mapOf("message" to privateChats.message, "timestamp" to System.currentTimeMillis()))
                                        .await()
                                }
                            }
                        }
                    }
                }
            }
        }.onFailure {
            it.message?.let { message ->
                message.logE(TAG)
            }
        }
    }

    companion object {
        private const val TAG = "ChatRepository"
    }

}