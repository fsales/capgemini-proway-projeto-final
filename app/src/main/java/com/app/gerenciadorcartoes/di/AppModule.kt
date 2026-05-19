package com.app.gerenciadorcartoes.di

import android.content.Context
import androidx.room.Room
import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.database.AppDatabase
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.repository.CartaoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindCartaoRepository(impl: CartaoRepositoryImpl): CartaoRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "gerenciador-cartoes-db",
            ).addMigrations(AppDatabase.MIGRATION_1_2).build()

        @Provides
        @Singleton
        fun provideCartaoDao(database: AppDatabase): CartaoDao = database.cartaoDao()
    }
}
