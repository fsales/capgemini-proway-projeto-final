package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.Cartao
import kotlinx.coroutines.flow.Flow

interface CartaoRepository {
    fun observarTodos(): Flow<List<Cartao>>
    /** Observação reativa de um único cartão — emite null quando excluído. */
    fun observarPorId(id: Long): Flow<Cartao?>
    suspend fun buscarPorId(id: Long): Cartao?
    suspend fun salvar(cartao: Cartao): Long
    suspend fun atualizar(cartao: Cartao)
    suspend fun atualizarLimite(id: Long, limite: Double)
    suspend fun excluirPorId(id: Long)
    suspend fun atualizarBloqueio(id: Long, novoStatusBloqueio: Boolean)

    // Envia uma representação do cartão para a API remota (se houver sessão ativa).
    suspend fun enviarParaApi(cartao: Cartao)

    // Solicita bloqueio/desbloqueio remoto e atualiza o banco local em sequência.
    suspend fun bloquearRemotamente(id: Long, novoStatusBloqueio: Boolean)
}
