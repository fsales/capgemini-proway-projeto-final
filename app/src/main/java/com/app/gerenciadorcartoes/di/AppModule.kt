package com.app.gerenciadorcartoes.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.app.gerenciadorcartoes.data.local.dao.CadastroUsuarioDao
import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.database.AppDatabase
import com.app.gerenciadorcartoes.data.local.session.SessionManager
import com.app.gerenciadorcartoes.data.local.session.SessionManagerImpl
import com.app.gerenciadorcartoes.repository.CadastroUsuarioRepository
import com.app.gerenciadorcartoes.repository.CadastroUsuarioRepositoryImpl
import com.app.gerenciadorcartoes.repository.CartaoDetalheRepository
import com.app.gerenciadorcartoes.repository.CartaoDetalheRepositoryImpl
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.repository.CartaoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindCartaoRepository(impl: CartaoRepositoryImpl): CartaoRepository

    @Binds @Singleton
    abstract fun bindCadastroUsuarioRepository(
        impl: CadastroUsuarioRepositoryImpl,
    ): CadastroUsuarioRepository

    @Binds @Singleton
    abstract fun bindCartaoDetalheRepository(
        impl: CartaoDetalheRepositoryImpl,
    ): CartaoDetalheRepository

    @Binds @Singleton
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    companion object {

        @Provides @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "gerenciador-cartoes-db",
            ).addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
            ).build()

        @Provides @Singleton
        fun provideCartaoDao(database: AppDatabase): CartaoDao = database.cartaoDao()

        @Provides @Singleton
        fun provideCadastroUsuarioDao(database: AppDatabase): CadastroUsuarioDao =
            database.cadastroUsuarioDao()

        @Provides @Singleton
        fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                scope       = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                produceFile = { context.preferencesDataStoreFile("session.preferences_pb") },
            )
    }
}
