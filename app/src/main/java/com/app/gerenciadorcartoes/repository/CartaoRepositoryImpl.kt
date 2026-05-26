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
            ?: throw IllegalStateException("Usuário não autenticado")
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
        // Erros de rede não propagam: o save local já foi concluído com sucesso.
    }
}
