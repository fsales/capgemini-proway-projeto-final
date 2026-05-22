package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gerenciadorcartoes.model.CadastroUsuario
import com.app.gerenciadorcartoes.network.service.BuscaCep
import com.app.gerenciadorcartoes.repository.CadastroUsuarioRepository
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioEvent
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioUiEvent
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.emailEstruturalmenteValido
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.formatCep
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.formatCpf
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.formatNumero
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.formatUf
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.onlyDigits
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.state.CadastroUsuarioUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CadastroUsuarioViewModel @Inject constructor(
    private val buscaCep                  : BuscaCep,
    private val cadastroUsuarioRepository : CadastroUsuarioRepository,
) : ViewModel() {

    companion object {
        private const val ETAPA_DADOS_PESSOAIS   = 0
        private const val ETAPA_ENDERECO         = 1
        private const val ETAPA_SEGURANCA        = 2
        private const val SENHA_MIN_LENGTH       = 6
        private const val MSG_CAMPO_OBRIGATORIO  = "Campo obrigatório"
        private const val MSG_EMAIL_INVALIDO     = "E-mail inválido"
        // Compilada uma única vez para toda a vida útil da aplicação
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    private val _uiState = MutableStateFlow(CadastroUsuarioUiState())
    val uiState: StateFlow<CadastroUsuarioUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<CadastroUsuarioUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: CadastroUsuarioEvent) {
        when (event) {
            is CadastroUsuarioEvent.NomeAlterado ->
                _uiState.update { it.copy(nome = event.valor, erroNome = null) }

            is CadastroUsuarioEvent.CpfAlterado -> {
                val formatted = formatCpf(event.valor)
                val digits = onlyDigits(formatted)
                _uiState.update {
                    it.copy(
                        cpf = formatted,
                        erroCpf = when {
                            formatted.isBlank() -> null
                            digits.length < 11  -> "CPF incompleto — ${digits.length}/11 dígitos"
                            else                -> null
                        },
                    )
                }
            }

            is CadastroUsuarioEvent.CepAlterado -> {
                val cepFormatado = formatCep(event.valor)
                val cepLimpo = onlyDigits(cepFormatado)
                _uiState.update {
                    it.copy(
                        cep = cepFormatado,
                        erroCep = when {
                            cepFormatado.isBlank() -> null
                            cepLimpo.length < 8    -> "CEP incompleto — ${cepLimpo.length}/8 dígitos"
                            else                   -> null
                        },
                    )
                }
                if (cepLimpo.length == 8) buscarCep(cepLimpo)
            }

            is CadastroUsuarioEvent.EnderecoAlterado ->
                _uiState.update { it.copy(endereco = event.valor, erroEndereco = null) }

            is CadastroUsuarioEvent.NumberAlterado ->
                _uiState.update { it.copy(number = formatNumero(event.valor), erroNumber = null) }

            is CadastroUsuarioEvent.BairroAlterado ->
                _uiState.update { it.copy(bairro = event.valor, erroBairro = null) }

            is CadastroUsuarioEvent.EstadoAlterado -> {
                val uf = formatUf(event.valor)
                _uiState.update {
                    it.copy(
                        estado = uf,
                        erroEstado = when {
                            uf.isBlank()  -> null
                            uf.length < 2 -> "Sigla incompleta — informe a UF (ex: SP)"
                            else          -> null
                        },
                    )
                }
            }

            is CadastroUsuarioEvent.EmailAlterado -> {
                val email = event.valor.trim()
                _uiState.update {
                    it.copy(
                        email = email,
                        erroEmail = when {
                            email.isBlank()  -> null
                            email.length < 3 -> null  // ainda digitando
                            else             -> if (EMAIL_REGEX.matches(email)) null else MSG_EMAIL_INVALIDO
                        },
                    )
                }
            }

            is CadastroUsuarioEvent.SenhaAlterada -> {
                _uiState.update {
                    it.copy(
                        senha = event.valor,
                        erroSenha = when {
                            event.valor.isBlank()            -> null
                            event.valor.length < SENHA_MIN_LENGTH ->
                                "Senha deve ter ao menos $SENHA_MIN_LENGTH caracteres"
                            else                             -> null
                        },
                        erroConfirmarSenha = confirmarSenhaErro(event.valor, it.confirmarSenha),
                    )
                }
            }

            is CadastroUsuarioEvent.ConfirmarSenhaAlterada -> {
                _uiState.update {
                    it.copy(
                        confirmarSenha = event.valor,
                        erroConfirmarSenha = confirmarSenhaErro(it.senha, event.valor),
                    )
                }
            }

            CadastroUsuarioEvent.AvancarEtapa  -> avancarEtapa()

            CadastroUsuarioEvent.VoltarEtapa   ->
                _uiState.update { it.copy(etapaAtual = (it.etapaAtual - 1).coerceAtLeast(0)) }

            CadastroUsuarioEvent.FocoRealizado ->
                _uiState.update { it.copy(focarPrimeiroCampoComErro = false) }

            CadastroUsuarioEvent.Voltar ->
                viewModelScope.launch { _uiEvent.send(CadastroUsuarioUiEvent.NavigateBack) }
        }
    }

    // ── Navegação de etapas ───────────────────────────────────────────────────

    /**
     * Valida a etapa atual. Se válida, avança para a próxima etapa ou, na última, submete o
     * cadastro. Se inválida, define erros inline e sinaliza foco via [CadastroUsuarioUiState.focarPrimeiroCampoComErro].
     */
    private fun avancarEtapa() {
        val etapa = _uiState.value.etapaAtual
        if (!validarEtapa(etapa)) return

        if (etapa < ETAPA_SEGURANCA) {
            _uiState.update { it.copy(etapaAtual = etapa + 1) }
        } else {
            submeterCadastro()
        }
    }

    /**
     * Valida todos os campos da [etapa] de uma só vez.
     * Em caso de falha, aplica erros e sinaliza [CadastroUsuarioUiState.focarPrimeiroCampoComErro] em um único
     * `_uiState.update` (evita múltiplas recomposições).
     *
     * @return `true` se todos os campos da etapa são válidos.
     */
    private fun validarEtapa(etapa: Int): Boolean {
        val s = _uiState.value
        return when (etapa) {
            ETAPA_DADOS_PESSOAIS -> {
                val erroNome  = if (s.nome.isBlank()) MSG_CAMPO_OBRIGATORIO else null
                val erroCpf   = when {
                    s.cpf.isBlank()              -> MSG_CAMPO_OBRIGATORIO
                    onlyDigits(s.cpf).length < 11 ->
                        "CPF incompleto — ${onlyDigits(s.cpf).length}/11 dígitos"
                    else                         -> null
                }
                val erroEmail = when {
                    s.email.isBlank()                    -> MSG_CAMPO_OBRIGATORIO
                    !emailEstruturalmenteValido(s.email) -> MSG_EMAIL_INVALIDO
                    !EMAIL_REGEX.matches(s.email)         -> MSG_EMAIL_INVALIDO
                    else                                 -> null
                }
                val valido = erroNome == null && erroCpf == null && erroEmail == null
                if (!valido) _uiState.update {
                    it.copy(
                        erroNome  = erroNome,
                        erroCpf   = erroCpf,
                        erroEmail = erroEmail,
                        focarPrimeiroCampoComErro = true,
                    )
                }
                valido
            }

            ETAPA_ENDERECO -> {
                val erroCep      = when {
                    s.cep.isBlank()             -> MSG_CAMPO_OBRIGATORIO
                    onlyDigits(s.cep).length < 8 ->
                        "CEP incompleto — ${onlyDigits(s.cep).length}/8 dígitos"
                    else                        -> null
                }
                val erroEndereco = if (s.endereco.isBlank()) MSG_CAMPO_OBRIGATORIO else null
                val erroNumber   = if (s.number.isBlank())   MSG_CAMPO_OBRIGATORIO else null
                val erroBairro   = if (s.bairro.isBlank())   MSG_CAMPO_OBRIGATORIO else null
                val erroEstado   = when {
                    s.estado.isBlank()  -> MSG_CAMPO_OBRIGATORIO
                    s.estado.length < 2 -> "Sigla incompleta — informe a UF (ex: SP)"
                    else                -> null
                }
                val valido = erroCep == null && erroEndereco == null && erroNumber == null &&
                             erroBairro == null && erroEstado == null
                if (!valido) _uiState.update {
                    it.copy(
                        erroCep      = erroCep,
                        erroEndereco = erroEndereco,
                        erroNumber   = erroNumber,
                        erroBairro   = erroBairro,
                        erroEstado   = erroEstado,
                        focarPrimeiroCampoComErro = true,
                    )
                }
                valido
            }

            ETAPA_SEGURANCA -> {
                val erroSenha = when {
                    s.senha.isBlank()             -> MSG_CAMPO_OBRIGATORIO
                    s.senha.length < SENHA_MIN_LENGTH ->
                        "Senha deve ter ao menos $SENHA_MIN_LENGTH caracteres"
                    else                          -> null
                }
                val erroConfirmarSenha = when {
                    s.confirmarSenha.isBlank()  -> MSG_CAMPO_OBRIGATORIO
                    s.senha != s.confirmarSenha -> "As senhas não coincidem"
                    else                        -> null
                }
                val valido = erroSenha == null && erroConfirmarSenha == null
                if (!valido) _uiState.update {
                    it.copy(
                        erroSenha          = erroSenha,
                        erroConfirmarSenha = erroConfirmarSenha,
                        focarPrimeiroCampoComErro = true,
                    )
                }
                valido
            }

            else -> false
        }
    }

    // ── Submissão ─────────────────────────────────────────────────────────────

    private fun submeterCadastro() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            val s = _uiState.value
            runCatching {
                // Verifica duplicidade de e-mail antes de persistir
                if (cadastroUsuarioRepository.buscarPorEmail(s.email) != null) {
                    _uiState.update {
                        it.copy(
                            carregando                = false,
                            erroEmail                 = "E-mail já cadastrado",
                            etapaAtual                = ETAPA_DADOS_PESSOAIS,
                            focarPrimeiroCampoComErro = true,
                        )
                    }
                    return@launch
                }
                cadastroUsuarioRepository.salvar(
                    CadastroUsuario(
                        nome     = s.nome.trim(),
                        cpf      = s.cpf,
                        cep      = s.cep,
                        endereco = s.endereco.trim(),
                        number   = s.number.trim(),
                        bairro   = s.bairro.trim(),
                        estado   = s.estado,
                        email    = s.email.trim(),
                        senha    = s.senha,
                    )
                )
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(CadastroUsuarioUiEvent.MostrarMensagem("Cadastro realizado com sucesso!"))
                _uiEvent.send(CadastroUsuarioUiEvent.NavigateBack)
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro(erro.message ?: "Erro ao cadastrar"))
            }
        }
    }

    // ── CEP ───────────────────────────────────────────────────────────────────

    private fun buscarCep(cep: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(buscandoCep = true) }
            runCatching {
                val resposta = buscaCep.getCep(cep)
                if (resposta.isSuccessful) {
                    val body = resposta.body()
                    if (body != null) {
                        _uiState.update {
                            it.copy(
                                endereco     = body.logradouro,
                                bairro       = body.bairro,
                                estado       = body.uf,
                                erroCep      = null,
                                erroEndereco = null,
                                erroBairro   = null,
                                erroEstado   = null,
                            )
                        }
                    } else {
                        _uiState.update { it.copy(erroCep = "CEP não encontrado") }
                    }
                } else {
                    _uiState.update { it.copy(erroCep = "CEP não encontrado") }
                }
            }.onFailure { erro ->
                _uiState.update { it.copy(erroCep = erro.message ?: "Erro ao buscar CEP") }
            }
            _uiState.update { it.copy(buscandoCep = false) }
        }
    }

    // ── Helpers de validação ──────────────────────────────────────────────────

    /** Retorna mensagem de erro se [confirmarSenha] não coincide com [senha], ou `null` se coincidem. */
    private fun confirmarSenhaErro(senha: String, confirmarSenha: String): String? {
        if (senha.isBlank() || confirmarSenha.isBlank()) return null
        return if (senha != confirmarSenha) "As senhas não coincidem" else null
    }
}
