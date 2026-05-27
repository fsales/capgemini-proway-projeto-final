/* ==== MERGE COMBINED: feature/sync-offline-fix (OURS) ==== */
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

    // Reposit├│rio apenas lida com dados; orquestra├º├úo de sync ├® responsabilidade de um coordinator

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
        // Gere um clientId para idempot├¬ncia (usado ao enviar para a API)
        val clientId = UUID.randomUUID().toString()

        // Inser├º├úo local imediata (offline-first) ÔÇö marca syncPending=true at├® a sincroniza├º├úo
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
        // Atualiza banco local ap├│s chamada remota
        cartaoDao.atualizarBloqueio(id, novoStatusBloqueio)
    }

    override suspend fun buscarPendentes(): List<Cartao> {
        return cartaoDao.buscarPendentes().map { it.toDomain() }
    }

    override suspend fun atualizarSyncPending(id: Long, pending: Boolean) {
        cartaoDao.atualizarSyncPending(id, pending)
    }

    override suspend fun sincronizarCartao(cartao: Cartao) {
        // Encapsula a l├│gica de envio remoto e atualiza├º├úo do estado local
        if (BuildConfig.DEBUG) {
            android.util.Log.d(
                "CartaoRepository",
                "Iniciando sincroniza├º├úo do cart├úo id=${cartao.id} clientId=${cartao.clientId}"
            )
        }
        try {
            enviarParaApi(cartao)
            // Marcar como sincronizado localmente
            cartaoDao.atualizarSyncPending(cartao.id, false)
            if (BuildConfig.DEBUG) {
                android.util.Log.d("CartaoRepository", "Cart├úo id=${cartao.id} sincronizado com sucesso")
            }
        } catch (e: Exception) {
            android.util.Log.w("CartaoRepository", "Falha ao sincronizar cart├úo id=${cartao.id}", e)
            // Propaga a exce├º├úo para o caller (Worker decide retry)
            throw e
        }
    }

    private suspend fun enviarParaApi(cartao: Cartao) {
        val userId = sessionManager.getSessionUserId().firstOrNull()
            ?: throw IllegalStateException("Sess├úo de usu├írio n├úo dispon├¡vel para sincroniza├º├úo")
        if (userId.isBlank()) throw IllegalStateException("Sess├úo de usu├írio vazia para sincroniza├º├úo")

        apiService.addCard(
            AddCardRequest(
                idUsuario = userId,
                // Prefer clientId para idempot├¬ncia, caia para id num├®rico se n├úo existir
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

/* ==== MERGE COMBINED: origin/main (THEIRS) ==== */
package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.data.local.session.SessionManager
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.network.model.AddCardRequest
import com.app.gerenciadorcartoes.network.model.BlockCardRequest
import com.app.gerenciadorcartoes.network.service.ApiService
import com.app.gerenciadorcartoes.repository.mapper.toDomain
import com.app.gerenciadorcartoes.repository.mapper.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class CartaoRepositoryImpl @Inject constructor(
    private val cartaoDao       : CartaoDao,
    private val apiService      : ApiService,
    private val sessionManager  : SessionManager,
) : CartaoRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun observarTodos(): Flow<List<Cartao>> =
        cartaoDao.observarTodos().map { list -> list.map { it.toDomain() } }

    override fun observarPorId(id: Long): Flow<Cartao?> =
        cartaoDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun buscarPorId(id: Long): Cartao? =
        cartaoDao.buscarPorId(id)?.toDomain()

    override suspend fun salvar(cartao: Cartao): Long {
        val id = cartaoDao.inserir(cartao.toEntity())
        syncScope.launch { sincronizarNovoCartao(cartao.copy(id = id)) }
        return id
    }

    override suspend fun atualizar(cartao: Cartao) =
        cartaoDao.atualizar(cartao.toEntity())

    override suspend fun atualizarLimite(id: Long, limite: Double) =
        cartaoDao.atualizarLimite(id, limite)

    override suspend fun atualizarBloqueio(id: Long, novoStatusBloqueio: Boolean) {
        val userId = sessionManager.getSessionUserId().firstOrNull()
            ?: throw IllegalStateException("Usu├írio n├úo autenticado")
        val request = BlockCardRequest(userId, id.toString())
        if (novoStatusBloqueio) {
            apiService.blockCard(request)
        } else {
            apiService.unblockCard(request)
        }
        cartaoDao.atualizarBloqueio(id, novoStatusBloqueio)
    }

    override suspend fun excluirPorId(id: Long) =
        cartaoDao.excluirPorId(id)

    override suspend fun enviarParaApi(cartao: Cartao) {
        // Reaproveita a l├│gica de sincroniza├º├úo usada ao inserir um cart├úo
        runCatching {
            val userId = sessionManager.getSessionUserId().firstOrNull() ?: return
            apiService.addCard(
                AddCardRequest(
                    idUsuario   = userId,
                    id          = cartao.id.toString(),
                    nomeTitular = cartao.nomeTitular,
                    finalNumero = cartao.finalNumero,
                    bandeira    = cartao.bandeira,
                    validade    = cartao.validade,
                    limite      = cartao.limite,
                    template    = cartao.template,
                    bloqueado   = cartao.bloqueado,
                ),
            )
        }
    }

    override suspend fun bloquearRemotamente(id: Long, novoStatusBloqueio: Boolean) {
        // Delega para a implementa├º├úo existente que j├í realiza a chamada remota
        // e atualiza o banco local em sequ├¬ncia.
        atualizarBloqueio(id, novoStatusBloqueio)
    }

    private suspend fun sincronizarNovoCartao(cartao: Cartao) {
        runCatching {
            val userId = sessionManager.getSessionUserId().firstOrNull() ?: return
            apiService.addCard(
                AddCardRequest(
                    idUsuario   = userId,
                    id          = cartao.id.toString(),
                    nomeTitular = cartao.nomeTitular,
                    finalNumero = cartao.finalNumero,
                    bandeira    = cartao.bandeira,
                    validade    = cartao.validade,
                    limite      = cartao.limite,
                    template    = cartao.template,
                    bloqueado   = cartao.bloqueado,
                ),
            )
        }
        // Erros de rede n├úo propagam: o save local j├í foi conclu├¡do com sucesso.
    }
}

