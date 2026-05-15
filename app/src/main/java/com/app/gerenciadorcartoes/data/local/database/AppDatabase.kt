package com.app.gerenciadorcartoes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity

@Database(
    entities     = [CartaoEntity::class],
    version      = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartaoDao(): CartaoDao
}
