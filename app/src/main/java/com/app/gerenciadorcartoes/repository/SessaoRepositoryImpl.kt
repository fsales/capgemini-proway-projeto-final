package com.app.gerenciadorcartoes.repository

import android.util.Log
import com.app.gerenciadorcartoes.data.local.session.SessionManager
import com.app.gerenciadorcartoes.model.ResultadoAutenticacaoExterna
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class SessaoRepositoryImpl @Inject constructor(
    private val authRepository             : AuthRepository,
    private val sessionManager             : SessionManager,
    private val cadastroUsuarioRepository  : CadastroUsuarioRepository,
) : SessaoRepository {

    override suspend fun entrar(email: String, senha: String) {
        val auth = authRepository.entrar(email, senha)
        sessionManager.saveSession(auth.userId)
    }

    override suspend fun autenticarComToken(idToken: String): ResultadoAutenticacaoExterna {
        val auth = authRepository.autenticarComToken(idToken)
        return resolverSessao(
            userId = auth.userId,
            email  = auth.email.orEmpty(),
            nome   = auth.nome.orEmpty(),
        )
    }

    override suspend fun criarContaFirebase(email: String, senha: String): String {
        val auth = authRepository.cadastrar(email, senha)
        return auth.userId
    }

    override suspend fun ativarSessao(userId: String) {
        sessionManager.saveSession(userId)
    }

    override suspend fun desfazerCriacaoConta() {
        runCatching { authRepository.deletarContaAtual() }
            .onFailure { erro ->
                Log.w("SessaoRepository", "Falha ao desfazer criação de conta no Firebase — conta pode ter ficado órfã", erro)
            }
    }

    override suspend fun encerrarSessao() {
        sessionManager.logout()
        authRepository.sair()
    }

    override suspend fun verificarSessaoInicial(): Boolean {
        val sessaoAtiva   = sessionManager.isLoggedIn().first()
        val firebaseAtivo = authRepository.usuarioAtual() != null

        return when {
            sessaoAtiva && firebaseAtivo  -> true
            sessaoAtiva && !firebaseAtivo -> {
                sessionManager.logout()
                false
            }
            else -> false
        }
    }

    override suspend fun verificarPerfilGoogleIncompleto(): ResultadoAutenticacaoExterna? {
        val sessaoAtiva  = sessionManager.isLoggedIn().first()
        val firebaseUser = authRepository.usuarioAtual()

        if (sessaoAtiva || firebaseUser == null) return null

        return resolverSessao(
            userId = firebaseUser.userId,
            email  = firebaseUser.email.orEmpty(),
            nome   = firebaseUser.nome.orEmpty(),
        )
    }

    // ── Dupla consulta: userId → e-mail → PrecisaCadastro ────────────────────
    // Cobre o caso em que a conta Firebase foi deletada e recriada via Google:
    // o UID muda, mas o perfil Room tem o mesmo e-mail com o UID antigo.
    private suspend fun resolverSessao(
        userId : String,
        email  : String,
        nome   : String,
    ): ResultadoAutenticacaoExterna {
        // 1ª consulta por userId
        val porId = cadastroUsuarioRepository.buscarPorUserId(userId)
        if (porId != null) {
            sessionManager.saveSession(userId)
            return ResultadoAutenticacaoExterna.Autenticado
        }

        // 2ª consulta por e-mail (UID recriado pelo Google)
        if (email.isNotBlank()) {
            val porEmail = cadastroUsuarioRepository.buscarPorEmail(email)
            if (porEmail != null) {
                cadastroUsuarioRepository.atualizarUserId(porEmail.id, userId)
                sessionManager.saveSession(userId)
                return ResultadoAutenticacaoExterna.Autenticado
            }
        }

        // Nenhum perfil encontrado: usuário novo
        return ResultadoAutenticacaoExterna.PrecisaCadastro(
            userId = userId,
            email  = email,
            nome   = nome,
        )
    }

    override fun observarDesconexaoExterna(): Flow<Unit> =
        authRepository.observarEstadoAuth().transform { usuario ->
            if (usuario == null && sessionManager.isLoggedIn().first()) {
                sessionManager.logout()
                emit(Unit)
            }
        }

    override suspend fun enviarRecuperacaoSenha(email: String) {
        authRepository.enviarRecuperacaoSenha(email)
    }

    override suspend fun buscarNomeUsuario(): String? {
        val userId = sessionManager.getSessionUserId().first() ?: return null
        return cadastroUsuarioRepository
            .buscarPorUserId(userId)
            ?.nome
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    override suspend fun buscarUserId(): String? =
        sessionManager.getSessionUserId().first()
}
