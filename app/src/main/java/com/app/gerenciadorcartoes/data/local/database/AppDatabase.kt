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
    version      = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartaoDao(): CartaoDao
    abstract fun cadastroUsuarioDao(): CadastroUsuarioDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE cartoes ADD COLUMN template TEXT NOT NULL DEFAULT 'default'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS CadastroUsuario (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        cpf TEXT NOT NULL,
                        cep INTEGER NOT NULL,
                        endereco TEXT NOT NULL,
                        number TEXT NOT NULL,
                        bairro TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        email TEXT NOT NULL,
                        senha TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
