package com.anogram.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anogram.app.data.local.dao.ChatDao
import com.anogram.app.data.local.dao.MessageDao
import com.anogram.app.data.local.dao.UserDao
import com.anogram.app.data.local.entity.ChatEntity
import com.anogram.app.data.local.entity.MessageEntity
import com.anogram.app.data.local.entity.UserEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnoGramDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "anogram_db"
    }
}
