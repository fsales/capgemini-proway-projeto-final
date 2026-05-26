package com.app.gerenciadorcartoes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.gerenciadorcartoes.data.local.entity.CadastroUsuarioEntity

@Dao
interface CadastroUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(entity: CadastroUsuarioEntity): Long

    @Update
    suspend fun atualizar(entity: CadastroUsuarioEntity)

    @Query("SELECT * FROM CadastroUsuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): CadastroUsuarioEntity?

    @Query("SELECT * FROM CadastroUsuario WHERE id = :id")
    suspend fun buscarPorId(id: Long): CadastroUsuarioEntity?

    @Query("SELECT * FROM CadastroUsuario WHERE userId = :userId LIMIT 1")
    suspend fun buscarPorUserId(userId: String): CadastroUsuarioEntity?

    /**
     * Atualiza o userId de um perfil existente.
     * Usado quando a conta Firebase foi deletada e recriada com o mesmo e-mail
     * via provider externo (Google), gerando um novo UID que precisa ser associado
     * ao perfil Room já existente.
     */
    @Query("UPDATE CadastroUsuario SET userId = :novoUserId WHERE id = :id")
    suspend fun atualizarUserId(id: Long, novoUserId: String)
}
