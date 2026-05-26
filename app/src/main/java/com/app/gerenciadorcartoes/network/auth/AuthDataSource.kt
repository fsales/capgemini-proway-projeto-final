package com.app.gerenciadorcartoes.network.auth

import com.app.gerenciadorcartoes.model.UsuarioAuth
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    suspend fun cadastrar(email: String, senha: String): UsuarioAuth
    suspend fun entrar(email: String, senha: String): UsuarioAuth
    suspend fun autenticarComToken(idToken: String): UsuarioAuth
    suspend fun enviarRecuperacaoSenha(email: String)
    suspend fun sair()
    suspend fun deletarContaAtual()
    fun usuarioAtual(): UsuarioAuth?
    fun observarEstadoAuth(): Flow<UsuarioAuth?>
}
