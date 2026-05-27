package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.ResultadoAutenticacaoExterna
import kotlinx.coroutines.flow.Flow

interface SessaoRepository {

    suspend fun entrar(email: String, senha: String)

    suspend fun autenticarComToken(idToken: String): ResultadoAutenticacaoExterna


    suspend fun criarContaFirebase(email: String, senha: String): String

    suspend fun ativarSessao(userId: String)

    suspend fun desfazerCriacaoConta()

    suspend fun encerrarSessao()

    suspend fun verificarSessaoInicial(): Boolean

    /**
     * Detecta o cenário em que o Firebase possui um usuário autenticado, mas a sessão local
     * (DataStore) não existe — ocorre quando o usuário iniciou o fluxo Google mas fechou o app
     * antes de completar o cadastro.
     *
     * - Retorna [ResultadoAutenticacaoExterna.PrecisaCadastro] → redirecionar para completar o perfil.
     * - Retorna [ResultadoAutenticacaoExterna.Autenticado] → usuário tem perfil em Room; sessão reparada automaticamente.
     * - Retorna `null` → situação normal, nenhuma ação especial necessária.
     */
    suspend fun verificarPerfilGoogleIncompleto(): ResultadoAutenticacaoExterna?

    fun observarDesconexaoExterna(): Flow<Unit>

    suspend fun enviarRecuperacaoSenha(email: String)

    /**
     * Retorna o nome completo do usuário da sessão ativa consultando o perfil em Room,
     * ou `null` se não houver sessão ativa ou o perfil não for encontrado.
     */
    suspend fun buscarNomeUsuario(): String?

    /***
     *  Retorna o id do usuário da sessão ativa consultando o perfil em Room,
     *  ou `null` se não houver sessão ativa ou o perfil não for encontrado.
     */
    suspend fun buscarIdUsuario(): Long?

    /**
     * Retorna o userId da sessão ativa (DataStore), ou `null` se não houver sessão.
     */
    suspend fun buscarUserId(): String?
}
