package com.app.gerenciadorcartoes.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ── Histórico de versões ──────────────────────────────────────────────────────
//
//  v1  Criação inicial da tabela `cartoes`
//  v2  Adicionado campo `template` em `cartoes`
//  v3  Criação da tabela `CadastroUsuario`
//         (cep INTEGER, inclui campo `senha`)
//  v4  Recriação de `CadastroUsuario`: cep INTEGER → TEXT
//         (SQLite não suporta ALTER COLUMN — requer recriação completa)
//  v5  Adicionado UNIQUE INDEX em `email` de `CadastroUsuario`
//  v6  Recriação de `CadastroUsuario`: removido `senha`, adicionado `userId`
//         + UNIQUE INDEX em `userId` (Firebase passa a gerenciar credenciais)
//  v7  Adicionado campo `cidade` em `CadastroUsuario` e `bloqueado` em `cartoes`
//  v8  Adicionado `limiteMaximo` em `cartoes` para preservar o limite cadastrado
//
// ─────────────────────────────────────────────────────────────────────────────

/** v1 → v2: adiciona `template` (TEXT NOT NULL DEFAULT 'default') em `cartoes`. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE cartoes ADD COLUMN template TEXT NOT NULL DEFAULT 'default'"
        )
    }
}

/**
 * v2 → v3: cria a tabela `CadastroUsuario`.
 * Nesta versão `cep` é INTEGER e existe o campo `senha`.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS CadastroUsuario (
                id       INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nome     TEXT    NOT NULL,
                cpf      TEXT    NOT NULL,
                cep      INTEGER NOT NULL,
                endereco TEXT    NOT NULL,
                number   TEXT    NOT NULL,
                bairro   TEXT    NOT NULL,
                estado   TEXT    NOT NULL,
                email    TEXT    NOT NULL,
                senha    TEXT    NOT NULL
            )
            """.trimIndent()
        )
    }
}

/**
 * v3 → v4: converte `cep` de INTEGER para TEXT.
 * SQLite não suporta ALTER COLUMN — a tabela é recriada com o tipo correto
 * e os dados existentes são copiados via CAST.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE CadastroUsuario RENAME TO CadastroUsuario_old")
        db.execSQL(
            """
            CREATE TABLE CadastroUsuario (
                id       INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nome     TEXT NOT NULL,
                cpf      TEXT NOT NULL,
                cep      TEXT NOT NULL,
                endereco TEXT NOT NULL,
                number   TEXT NOT NULL,
                bairro   TEXT NOT NULL,
                estado   TEXT NOT NULL,
                email    TEXT NOT NULL,
                senha    TEXT NOT NULL
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

/** v4 → v5: adiciona UNIQUE INDEX no campo `email` de `CadastroUsuario`. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_CadastroUsuario_email ON CadastroUsuario(email)"
        )
    }
}

/**
 * v5 → v6: remove o campo `senha` e adiciona `userId`.
 * Firebase Auth passa a ser o único responsável pelas credenciais.
 * SQLite não suporta DROP COLUMN — a tabela é recriada sem `senha` e com `userId`.
 * Os índices únicos (`email` e `userId`) são recriados ao final.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Renomeia a tabela antiga
        db.execSQL("ALTER TABLE CadastroUsuario RENAME TO CadastroUsuario_old")

        // 2. Cria a nova tabela sem `senha` e com `userId`
        db.execSQL(
            """
            CREATE TABLE CadastroUsuario (
                id       INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId   TEXT    NOT NULL DEFAULT '',
                nome     TEXT    NOT NULL,
                cpf      TEXT    NOT NULL,
                cep      TEXT    NOT NULL,
                endereco TEXT    NOT NULL,
                number   TEXT    NOT NULL,
                bairro   TEXT    NOT NULL,
                estado   TEXT    NOT NULL,
                email    TEXT    NOT NULL
            )
            """.trimIndent()
        )

        // 3. Copia os dados (userId recebe string vazia — será preenchido no login)
        db.execSQL(
            """
            INSERT INTO CadastroUsuario (id, userId, nome, cpf, cep, endereco, number, bairro, estado, email)
            SELECT id, '', nome, cpf, cep, endereco, number, bairro, estado, email
            FROM CadastroUsuario_old
            """.trimIndent()
        )

        // 4. Remove a tabela antiga (o índice email antigo é removido junto)
        db.execSQL("DROP TABLE CadastroUsuario_old")

        // 5. Recria os índices únicos
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_CadastroUsuario_email  ON CadastroUsuario(email)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_CadastroUsuario_userId ON CadastroUsuario(userId)"
        )
    }
}

/**
 * v6 → v7: adiciona `cidade` em `CadastroUsuario` e `bloqueado` em `cartoes`.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("CadastroUsuario", "cidade")) {
            db.execSQL(
                "ALTER TABLE CadastroUsuario ADD COLUMN cidade TEXT NOT NULL DEFAULT ''"
            )
        }
        if (!db.hasColumn("cartoes", "bloqueado")) {
            db.execSQL(
                "ALTER TABLE cartoes ADD COLUMN bloqueado INTEGER NOT NULL DEFAULT 0"
            )
        }
        db.recreateCadastroUsuarioTable()
        db.recreateCartoesTable(includeLimiteMaximo = false)
    }
}

/**
 * v7 → v8: adiciona `limiteMaximo` em `cartoes`.
 * Para cartões existentes, o limite atual passa a ser o teto cadastrado.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!db.hasColumn("cartoes", "bloqueado")) {
            db.execSQL(
                "ALTER TABLE cartoes ADD COLUMN bloqueado INTEGER NOT NULL DEFAULT 0"
            )
        }
        if (!db.hasColumn("cartoes", "limiteMaximo")) {
            db.execSQL(
                "ALTER TABLE cartoes ADD COLUMN limiteMaximo REAL NOT NULL DEFAULT 0.0"
            )
        }
        db.execSQL(
            "UPDATE cartoes SET limiteMaximo = limite WHERE limiteMaximo = 0.0"
        )
        db.recreateCartoesTable(includeLimiteMaximo = true)
    }
}

/** Lista completa de migrações — passe para `.addMigrations(*ALL_MIGRATIONS)`. */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
)

private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean {
    query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) return true
        }
    }
    return false
}

private fun SupportSQLiteDatabase.recreateCartoesTable(includeLimiteMaximo: Boolean) {
    execSQL("DROP TABLE IF EXISTS cartoes_new")

    val limiteMaximoColumn = if (includeLimiteMaximo) "`limiteMaximo` REAL NOT NULL, " else ""
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS cartoes_new (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `nomeTitular` TEXT NOT NULL,
            `finalNumero` TEXT NOT NULL,
            `bandeira` TEXT NOT NULL,
            `validade` TEXT NOT NULL,
            `limite` REAL NOT NULL,
            $limiteMaximoColumn
            `template` TEXT NOT NULL,
            `bloqueado` INTEGER NOT NULL
        )
        """.trimIndent()
    )

    val limiteMaximoInsert = if (includeLimiteMaximo) "limiteMaximo, " else ""
    val limiteMaximoSelect = if (includeLimiteMaximo) "limiteMaximo, " else ""
    execSQL(
        """
        INSERT INTO cartoes_new (
            id, nomeTitular, finalNumero, bandeira, validade, limite,
            ${limiteMaximoInsert}template, bloqueado
        )
        SELECT
            id, nomeTitular, finalNumero, bandeira, validade, limite,
            ${limiteMaximoSelect}template, bloqueado
        FROM cartoes
        """.trimIndent()
    )

    execSQL("DROP TABLE cartoes")
    execSQL("ALTER TABLE cartoes_new RENAME TO cartoes")
}

private fun SupportSQLiteDatabase.recreateCadastroUsuarioTable() {
    execSQL("DROP TABLE IF EXISTS CadastroUsuario_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS CadastroUsuario_new (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `userId` TEXT NOT NULL,
            `nome` TEXT NOT NULL,
            `cpf` TEXT NOT NULL,
            `cep` TEXT NOT NULL,
            `endereco` TEXT NOT NULL,
            `number` TEXT NOT NULL,
            `bairro` TEXT NOT NULL,
            `cidade` TEXT NOT NULL,
            `estado` TEXT NOT NULL,
            `email` TEXT NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        INSERT INTO CadastroUsuario_new (
            id, userId, nome, cpf, cep, endereco, number, bairro, cidade, estado, email
        )
        SELECT
            id, userId, nome, cpf, cep, endereco, number, bairro, cidade, estado, email
        FROM CadastroUsuario
        """.trimIndent()
    )
    execSQL("DROP TABLE CadastroUsuario")
    execSQL("ALTER TABLE CadastroUsuario_new RENAME TO CadastroUsuario")
    execSQL(
        "CREATE UNIQUE INDEX IF NOT EXISTS index_CadastroUsuario_email ON CadastroUsuario(email)"
    )
    execSQL(
        "CREATE UNIQUE INDEX IF NOT EXISTS index_CadastroUsuario_userId ON CadastroUsuario(userId)"
    )
}
