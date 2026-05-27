package com.app.gerenciadorcartoes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartaoDao {

    @Query("SELECT * FROM cartoes ORDER BY nomeTitular ASC")
    fun observarTodos(): Flow<List<CartaoEntity>>

    /** Observação reativa de um único cartão — emite sempre que o registro for alterado. */
    @Query("SELECT * FROM cartoes WHERE id = :id")
    fun observarPorId(id: Long): Flow<CartaoEntity?>

    @Query("SELECT * FROM cartoes WHERE cadastroUsuarioId = :cadastroUsuarioId ORDER BY nomeTitular ASC")
    fun buscarCartaosPorUsuario(cadastroUsuarioId: Long): Flow<List<CartaoEntity>>

    /** Leitura pontual — use apenas quando não precisar de reatividade. */
    @Query("SELECT * FROM cartoes WHERE id = :id")
    suspend fun buscarPorId(id: Long): CartaoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(entity: CartaoEntity): Long

    @Update
    suspend fun atualizar(entity: CartaoEntity)

    @Query("SELECT * FROM cartoes WHERE syncPending = 1")
    suspend fun buscarPendentes(): List<CartaoEntity>

    @Query("UPDATE cartoes SET syncPending = :pending WHERE id = :id")
    suspend fun atualizarSyncPending(id: Long, pending: Boolean)

    @Query("UPDATE cartoes SET limite = :limite WHERE id = :id")
    suspend fun atualizarLimite(id: Long, limite: Double)

    @Query("UPDATE cartoes SET bloqueado = :bloqueado WHERE id = :id")
    suspend fun atualizarBloqueio(id: Long, bloqueado: Boolean)

    @Query("DELETE FROM cartoes WHERE id = :id")
    suspend fun excluirPorId(id: Long)
}
