package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.data.local.dao.CartaoDao
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.repository.mapper.toDomain
import com.app.gerenciadorcartoes.repository.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartaoRepositoryImpl @Inject constructor(
    private val cartaoDao: CartaoDao,
) : CartaoRepository {

    override fun observarTodos(): Flow<List<Cartao>> =
        cartaoDao.observarTodos().map { list -> list.map { it.toDomain() } }

    override fun observarPorId(id: Long): Flow<Cartao?> =
        cartaoDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun buscarPorId(id: Long): Cartao? =
        cartaoDao.buscarPorId(id)?.toDomain()

    override suspend fun salvar(cartao: Cartao): Long =
        cartaoDao.inserir(cartao.toEntity())

    override suspend fun atualizar(cartao: Cartao) =
        cartaoDao.atualizar(cartao.toEntity())

    override suspend fun excluirPorId(id: Long) =
        cartaoDao.excluirPorId(id)
}
