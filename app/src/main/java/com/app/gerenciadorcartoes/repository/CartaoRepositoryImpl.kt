package com.app.gerenciadorcartoes.repository

import android.content.Context
import android.util.Log
import com.app.gerenciadorcartoes.BuildConfig
import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.session.SessionManager
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.network.model.AddCardRequest
import com.app.gerenciadorcartoes.network.model.BlockCardRequest
import com.app.gerenciadorcartoes.network.service.ApiService
import com.app.gerenciadorcartoes.repository.mapper.toDomain
import com.app.gerenciadorcartoes.repository.mapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class CartaoRepositoryImpl @Inject constructor(
    private val cartaoDao: CartaoDao,
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    @ApplicationContext private val appContext: Context,
) : CartaoRepository {

    // Repositório apenas lida com dados; orquestração de sync é responsabilidade de um coordinator

    override fun observarTodos(): Flow<List<Cartao>> =
        cartaoDao.observarTodos().map { list -> list.map { it.toDomain() } }

    override fun buscarCartaosPorUsuario(idUsuario: Long?): Flow<List<Cartao>> {
        val id = idUsuario ?: return flowOf(emptyList())
        return cartaoDao.buscarCartaosPorUsuario(id).map { it.map { e -> e.toDomain() } }
    }

    override fun observarPorId(id: Long): Flow<Cartao?> =
        cartaoDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun buscarPorId(id: Long): Cartao? =
        cartaoDao.buscarPorId(id)?.toDomain()

    override suspend fun salvar(cartao: Cartao): Long {
        // Gere um clientId para idempotência (usado ao enviar para a API)
        val clientId = UUID.randomUUID().toString()

        // Inserção local imediata (offline-first) — marca syncPending=true até a sincronização
        val novoId = cartaoDao.inserir(cartao.toEntity(clientId = clientId, syncPending = true))
        return novoId
    }

    override suspend fun atualizar(cartao: Cartao) =
        cartaoDao.atualizar(cartao.toEntity())

    override suspend fun atualizarLimite(id: Long, limite: Double) =
        cartaoDao.atualizarLimite(id, limite)

    override suspend fun atualizarBloqueio(id: Long, novoStatusBloqueio: Boolean) =
        cartaoDao.atualizarBloqueio(id, novoStatusBloqueio)

    override suspend fun excluirPorId(id: Long) =
        cartaoDao.excluirPorId(id)

    override suspend fun bloquearRemotamente(id: Long, novoStatusBloqueio: Boolean) {
        val userId = sessionManager.getSessionUserId().firstOrNull() ?: return
        val request = BlockCardRequest(userId, id.toString())
        if (novoStatusBloqueio) apiService.blockCard(request) else apiService.unblockCard(request)
        // Atualiza banco local após chamada remota
        cartaoDao.atualizarBloqueio(id, novoStatusBloqueio)
    }

    override suspend fun buscarPendentes(): List<Cartao> {
        return cartaoDao.buscarPendentes().map { it.toDomain() }
    }

    override suspend fun atualizarSyncPending(id: Long, pending: Boolean) {
        cartaoDao.atualizarSyncPending(id, pending)
    }

    override suspend fun sincronizarCartao(cartao: Cartao) {
        // Encapsula a lógica de envio remoto e atualização do estado local
        if (BuildConfig.DEBUG) {
            android.util.Log.d(
                "CartaoRepository",
                "Iniciando sincronização do cartão id=${cartao.id} clientId=${cartao.clientId}"
            )
        }
        try {
            enviarParaApi(cartao)
            // Marcar como sincronizado localmente
            cartaoDao.atualizarSyncPending(cartao.id, false)
            if (BuildConfig.DEBUG) {
                android.util.Log.d("CartaoRepository", "Cartão id=${cartao.id} sincronizado com sucesso")
            }
        } catch (e: Exception) {
            android.util.Log.w("CartaoRepository", "Falha ao sincronizar cartão id=${cartao.id}", e)
            // Propaga a exceção para o caller (Worker decide retry)
            throw e
        }
    }

    private suspend fun enviarParaApi(cartao: Cartao) {
        val userId = sessionManager.getSessionUserId().firstOrNull()
            ?: throw IllegalStateException("Sessão de usuário não disponível para sincronização")
        if (userId.isBlank()) throw IllegalStateException("Sessão de usuário vazia para sincronização")

        apiService.addCard(
            AddCardRequest(
                idUsuario = userId,
                // Prefer clientId para idempotência, caia para id numérico se não existir
                id = cartao.clientId ?: cartao.id.toString(),
                nomeTitular = cartao.nomeTitular,
                finalNumero = cartao.finalNumero,
                bandeira = cartao.bandeira,
                validade = cartao.validade,
                limite = cartao.limite,
                template = cartao.template,
                bloqueado = cartao.bloqueado,
            )
        )
    }
}
