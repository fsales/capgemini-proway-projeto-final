package com.app.gerenciadorcartoes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.dao.CadastroUsuarioDao
import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity
import com.app.gerenciadorcartoes.data.local.entity.CadastroUsuarioEntity

@Database(
    entities     = [CartaoEntity::class, CadastroUsuarioEntity::class],
    version      = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartaoDao(): CartaoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE cartoes ADD COLUMN template TEXT NOT NULL DEFAULT 'default'"
                )
            }
        }
    }
    abstract fun cadastroUsuarioDao(): CadastroUsuarioDao
}
