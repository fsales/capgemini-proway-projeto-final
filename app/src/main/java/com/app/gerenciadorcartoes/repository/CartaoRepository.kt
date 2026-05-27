package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.Cartao
import kotlinx.coroutines.flow.Flow

interface CartaoRepository {
    fun observarTodos(): Flow<List<Cartao>>
    fun buscarCartaosPorUsuario(idUsuario: Long?): Flow<List<Cartao>>
    /** Observação reativa de um único cartão — emite null quando excluído. */
    fun observarPorId(id: Long): Flow<Cartao?>
    suspend fun buscarPorId(id: Long): Cartao?
    suspend fun salvar(cartao: Cartao): Long
    suspend fun atualizar(cartao: Cartao)
    suspend fun atualizarLimite(id: Long, limite: Double)
    suspend fun excluirPorId(id: Long)
    suspend fun atualizarBloqueio(id: Long, novoStatusBloqueio: Boolean)
    // Retorna lista de cartões que estão pendentes de sincronização com a API
    suspend fun buscarPendentes(): List<Cartao>
    // Atualiza a flag de sincronização para um registro local
    suspend fun atualizarSyncPending(id: Long, pending: Boolean)
    // Sincroniza um cartão específico com a API remota (idempotente usando clientId)
    // Deve atualizar o estado local (syncPending=false) em caso de sucesso.
    suspend fun sincronizarCartao(cartao: Cartao)

    // Solicita bloqueio/desbloqueio remoto e atualiza o banco local em sequência.
    suspend fun bloquearRemotamente(id: Long, novoStatusBloqueio: Boolean)
}
