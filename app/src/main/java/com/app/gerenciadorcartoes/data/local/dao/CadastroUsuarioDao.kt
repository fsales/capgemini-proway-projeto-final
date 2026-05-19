package com.app.gerenciadorcartoes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.gerenciadorcartoes.data.local.entity.CadastroUsuarioEntity

@Dao
interface CadastroUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(entity: CadastroUsuarioEntity): Long

    @Query("SELECT * FROM CadastroUsuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): CadastroUsuarioEntity?

    @Query("SELECT * FROM CadastroUsuario WHERE id = :id")
    suspend fun buscarPorId(id: Long): CadastroUsuarioEntity?
}

