package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.data.local.dao.CadastroUsuarioDao
import com.app.gerenciadorcartoes.data.local.security.SenhaHasher
import com.app.gerenciadorcartoes.model.CadastroUsuario
import com.app.gerenciadorcartoes.repository.mapper.toDomain
import com.app.gerenciadorcartoes.repository.mapper.toEntity
import javax.inject.Inject

class CadastroUsuarioRepositoryImpl @Inject constructor(
    private val cadastroUsuarioDao: CadastroUsuarioDao,
) : CadastroUsuarioRepository {

    /** Persiste o usuário com a senha **hasheada** — nunca armazena texto plano. */
    override suspend fun salvar(usuario: CadastroUsuario): Long =
        cadastroUsuarioDao.inserir(
            usuario.copy(senha = SenhaHasher.hash(usuario.senha)).toEntity()
        )

    override suspend fun buscarPorEmail(email: String): CadastroUsuario? =
        cadastroUsuarioDao.buscarPorEmail(email)?.toDomain()

    override suspend fun buscarPorId(id: Long): CadastroUsuario? =
        cadastroUsuarioDao.buscarPorId(id)?.toDomain()

    override suspend fun verificarCredenciais(email: String, senha: String): Boolean {
        val entity = cadastroUsuarioDao.buscarPorEmail(email) ?: return false
        return SenhaHasher.verificar(senha, entity.senha)
    }
}
