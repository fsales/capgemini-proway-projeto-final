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
    version      = 5,
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

        /**
         * v3 → v4: altera `cep` de INTEGER para TEXT na tabela CadastroUsuario.
         * SQLite não suporta ALTER COLUMN — a tabela é recriada com o tipo correto.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CadastroUsuario RENAME TO CadastroUsuario_old")
                db.execSQL(
                    """
                    CREATE TABLE CadastroUsuario (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        cpf TEXT NOT NULL,
                        cep TEXT NOT NULL,
                        endereco TEXT NOT NULL,
                        number TEXT NOT NULL,
                        bairro TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        email TEXT NOT NULL,
                        senha TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO CadastroUsuario
                    SELECT id, nome, cpf, CAST(cep AS TEXT), endereco, number, bairro, estado, email, senha
                    FROM CadastroUsuario_old
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE CadastroUsuario_old")
            }
        }

        /**
         * v4 → v5: cria índice UNIQUE no campo `email` da tabela CadastroUsuario.
         * Garante unicidade em nível de banco, prevenindo inserções duplicadas
         * independente de race conditions na camada de aplicação.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_CadastroUsuario_email ON CadastroUsuario(email)"
                )
            }
        }
    }
}
