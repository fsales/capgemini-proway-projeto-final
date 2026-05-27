package com.app.gerenciadorcartoes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.gerenciadorcartoes.data.local.dao.UsuarioDao
import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.entity.UsuarioEntity
import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity

@Database(
    entities     = [CartaoEntity::class, UsuarioEntity::class],
    version      = 11,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartaoDao(): CartaoDao
    abstract fun cadastroUsuarioDao(): UsuarioDao
}
