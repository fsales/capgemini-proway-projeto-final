package com.app.gerenciadorcartoes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.gerenciadorcartoes.data.local.dao.CadastroUsuarioDao
import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.entity.CadastroUsuarioEntity
import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity

@Database(
    entities     = [CartaoEntity::class, CadastroUsuarioEntity::class],
    version      = 10,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartaoDao(): CartaoDao
    abstract fun cadastroUsuarioDao(): CadastroUsuarioDao
}
