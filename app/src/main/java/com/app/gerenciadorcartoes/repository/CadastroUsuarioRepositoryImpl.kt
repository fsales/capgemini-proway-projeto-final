package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.data.local.dao.CadastroUsuarioDao
import com.app.gerenciadorcartoes.model.CadastroUsuario
import com.app.gerenciadorcartoes.repository.mapper.toDomain
import com.app.gerenciadorcartoes.repository.mapper.toEntity
import javax.inject.Inject

class CadastroUsuarioRepositoryImpl @Inject constructor(
    private val cadastroUsuarioDao: CadastroUsuarioDao,
) : CadastroUsuarioRepository {

    override suspend fun salvar(usuario: CadastroUsuario): Long =
        cadastroUsuarioDao.inserir(usuario.toEntity())

    override suspend fun atualizar(usuario: CadastroUsuario) =
        cadastroUsuarioDao.atualizar(usuario.toEntity())

    override suspend fun buscarPorEmail(email: String): CadastroUsuario? =
        cadastroUsuarioDao.buscarPorEmail(email)?.toDomain()

    override suspend fun buscarPorId(id: Long): CadastroUsuario? =
        cadastroUsuarioDao.buscarPorId(id)?.toDomain()

    override suspend fun buscarPorUserId(userId: String): CadastroUsuario? =
        cadastroUsuarioDao.buscarPorUserId(userId)?.toDomain()

    override suspend fun atualizarUserId(id: Long, novoUserId: String) =
        cadastroUsuarioDao.atualizarUserId(id, novoUserId)
}
