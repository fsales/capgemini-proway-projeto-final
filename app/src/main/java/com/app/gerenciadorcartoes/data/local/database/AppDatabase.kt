package com.app.gerenciadorcartoes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.dao.CadastroUsuarioDao
import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity
import com.app.gerenciadorcartoes.data.local.entity.CadastroUsuarioEntity

@Database(
    entities     = [CartaoEntity::class, CadastroUsuarioEntity::class],
    version      = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartaoDao(): CartaoDao
    abstract fun cadastroUsuarioDao(): CadastroUsuarioDao
}
