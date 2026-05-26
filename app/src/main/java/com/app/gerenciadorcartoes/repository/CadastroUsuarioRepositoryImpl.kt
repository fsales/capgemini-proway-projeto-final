package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.data.local.dao.CadastroUsuarioDao
import com.app.gerenciadorcartoes.model.CadastroUsuario
import com.app.gerenciadorcartoes.model.Endereco
import com.app.gerenciadorcartoes.network.service.BuscaCep
import com.app.gerenciadorcartoes.repository.mapper.toDomain
import com.app.gerenciadorcartoes.repository.mapper.toEntity
import javax.inject.Inject

class CadastroUsuarioRepositoryImpl @Inject constructor(
    private val cadastroUsuarioDao: CadastroUsuarioDao,
    private val buscaCep: BuscaCep,
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

    override suspend fun buscarEnderecoPorCep(cep: String): Endereco? {
        return try {
            val response = buscaCep.getCep(cep)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Endereco(
                        logradouro = body.logradouro,
                        bairro = body.bairro,
                        cidade = body.localidade,
                        estado = body.uf,
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
