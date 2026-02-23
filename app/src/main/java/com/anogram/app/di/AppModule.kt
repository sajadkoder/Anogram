package com.anogram.app.di

import android.content.Context
import androidx.room.Room
import com.anogram.app.data.local.AnoGramDatabase
import com.anogram.app.data.local.dao.ChatDao
import com.anogram.app.data.local.dao.MessageDao
import com.anogram.app.data.local.dao.UserDao
import com.anogram.app.data.repository.ChatRepositoryImpl
import com.anogram.app.data.repository.MessageRepositoryImpl
import com.anogram.app.data.repository.UserRepositoryImpl
import com.anogram.app.domain.repository.ChatRepository
import com.anogram.app.domain.repository.MessageRepository
import com.anogram.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnoGramDatabase {
        return Room.databaseBuilder(
            context,
            AnoGramDatabase::class.java,
            AnoGramDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideChatDao(database: AnoGramDatabase): ChatDao = database.chatDao()

    @Provides
    fun provideMessageDao(database: AnoGramDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideUserDao(database: AnoGramDatabase): UserDao = database.userDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
