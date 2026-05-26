package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.network.auth.AuthDataSource
import com.app.gerenciadorcartoes.model.UsuarioAuth
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
) : AuthRepository {

    override suspend fun cadastrar(email: String, senha: String): UsuarioAuth =
        authDataSource.cadastrar(email, senha)

    override suspend fun entrar(email: String, senha: String): UsuarioAuth =
        authDataSource.entrar(email, senha)

    override suspend fun autenticarComToken(idToken: String): UsuarioAuth =
        authDataSource.autenticarComToken(idToken)


    override suspend fun enviarRecuperacaoSenha(email: String) =
        authDataSource.enviarRecuperacaoSenha(email)

    override suspend fun sair() = authDataSource.sair()

    override suspend fun deletarContaAtual() = authDataSource.deletarContaAtual()

    override fun usuarioAtual(): UsuarioAuth? = authDataSource.usuarioAtual()

    override fun observarEstadoAuth(): Flow<UsuarioAuth?> = authDataSource.observarEstadoAuth()
}
