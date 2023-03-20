package com.givebox.data.local.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.givebox.data.local.db.dao.ChatDao
import com.givebox.data.local.db.dao.ProductCategoryDao
import com.givebox.data.local.db.dao.ProductDao
import com.givebox.data.local.db.dao.UserDao
import com.givebox.data.local.db.entity.*

/**
 * Created by Fasil on 20/11/22.
 */
@Database(
    entities = [UserEntity::class, ProductEntity::class,
        ProductCategoryEntity::class, ChatsEntity::class,
        PrivateChatsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun productCategoryDao(): ProductCategoryDao
    abstract fun chatDao(): ChatDao

    companion object {
        fun getInstance(application: Application): AppDatabase {
            return Room.databaseBuilder(
                application,
                AppDatabase::class.java,
                "givebox.db"
            ).build()
        }
    }
}