package com.app.gerenciadorcartoes.di

import com.app.gerenciadorcartoes.BuildConfig
import com.app.gerenciadorcartoes.repository.ChatRepository
import com.app.gerenciadorcartoes.repository.MastraChatRepositoryImpl
import com.app.gerenciadorcartoes.repository.MockChatRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides @Singleton
    fun provideChatRepository(
        mockRepository   : MockChatRepositoryImpl,
        mastraRepository : MastraChatRepositoryImpl,
    ): ChatRepository =
        if (BuildConfig.CHAT_USE_MOCK) {
            mockRepository
        } else {
            mastraRepository
        }
}
