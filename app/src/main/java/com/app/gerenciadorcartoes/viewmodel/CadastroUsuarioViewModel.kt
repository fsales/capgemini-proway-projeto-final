package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.model.CadastroUsuario
import com.app.gerenciadorcartoes.repository.CadastroUsuarioRepository
import com.app.gerenciadorcartoes.repository.SessaoRepository
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioEvent
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioUiEvent
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.state.CadastroUsuarioUiState
import com.app.gerenciadorcartoes.ui.navigation.CadastroUsuarioRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
    savedStateHandle                      : SavedStateHandle,
    private val sessaoRepository          : SessaoRepository,
    private val cadastroUsuarioRepository : CadastroUsuarioRepository,
) : ViewModel() {

    private val route: CadastroUsuarioRoute = savedStateHandle.toRoute<CadastroUsuarioRoute>()

    companion object {
        private const val ETAPA_DADOS_PESSOAIS   = 0
        private const val ETAPA_ENDERECO         = 1
        private const val ETAPA_SEGURANCA        = 2
        private const val SENHA_MIN_LENGTH       = 6
        private const val MSG_CAMPO_OBRIGATORIO  = "Campo obrigatório"
        private const val MSG_EMAIL_INVALIDO     = "E-mail inválido"
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    private val _uiState = MutableStateFlow(CadastroUsuarioUiState())
    val uiState: StateFlow<CadastroUsuarioUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<CadastroUsuarioUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        when {
            route.modoEdicao && route.userId.isNotEmpty() -> carregarPerfil()
            route.userId.isNotEmpty() -> _uiState.update {
                it.copy(
                    email          = route.emailExterno,
                    nome           = route.nomeExterno,
                    isFluxoExterno = true,
                )
            }
        }
    }

    fun onEvent(event: CadastroUsuarioEvent) {
        when (event) {
            is CadastroUsuarioEvent.NomeAlterado ->
                _uiState.update { it.copy(nome = event.valor, erroNome = null) }

            is CadastroUsuarioEvent.CpfAlterado -> {
                val digits = event.valor.filter { it.isDigit() }.take(11)
                _uiState.update {
                    it.copy(
                        cpf = digits,
                        erroCpf = when {
                            digits.isBlank()   -> null
                            digits.length < 11 -> "CPF incompleto — ${digits.length}/11 dígitos"
                            else               -> null
                        },
                    )
                }
            }

            is CadastroUsuarioEvent.CepAlterado -> {
                val digits = event.valor.filter { it.isDigit() }.take(8)
                _uiState.update {
                    it.copy(
                        cep = digits,
                        erroCep = when {
                            digits.isBlank()  -> null
                            digits.length < 8 -> "CEP incompleto — ${digits.length}/8 dígitos"
                            else              -> null
                        },
                    )
                }
                if (digits.length == 8) buscarCep(digits)
            }

            is CadastroUsuarioEvent.EnderecoAlterado ->
                _uiState.update { it.copy(endereco = event.valor, erroEndereco = null) }

            is CadastroUsuarioEvent.NumberAlterado ->
                _uiState.update { it.copy(number = formatNumero(event.valor), erroNumber = null) }

            is CadastroUsuarioEvent.BairroAlterado ->
                _uiState.update { it.copy(bairro = event.valor, erroBairro = null) }

            is CadastroUsuarioEvent.CidadeAlterada ->
                _uiState.update { it.copy(cidade = event.valor, erroCidade = null) }

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
                            email.length < 3 -> null
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
                            event.valor.isBlank()                     -> null
                            event.valor.length < SENHA_MIN_LENGTH ->
                                "Senha deve ter ao menos $SENHA_MIN_LENGTH caracteres"
                            else                                      -> null
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

    private fun avancarEtapa() {
        val etapa = _uiState.value.etapaAtual
        if (!validarEtapa(etapa)) return

        // Fluxo externo (Google) e modo edição terminam no ETAPA_ENDERECO — sem passo de senha.
        val ultimaEtapa = if (route.modoEdicao || route.userId.isNotEmpty()) ETAPA_ENDERECO
                          else                                               ETAPA_SEGURANCA

        if (etapa < ultimaEtapa) {
            _uiState.update { it.copy(etapaAtual = etapa + 1) }
        } else {
            submeterCadastro()
        }
    }

    private fun validarEtapa(etapa: Int): Boolean {
        val s = _uiState.value
        return when (etapa) {
            ETAPA_DADOS_PESSOAIS -> {
                val erroNome  = if (s.nome.isBlank()) MSG_CAMPO_OBRIGATORIO else null
                val erroCpf   = when {
                    s.cpf.isBlank()    -> MSG_CAMPO_OBRIGATORIO
                    s.cpf.length < 11  -> "CPF incompleto — ${s.cpf.length}/11 dígitos"
                    else               -> null
                }
                val erroEmail = when {
                    s.email.isBlank()             -> MSG_CAMPO_OBRIGATORIO
                    !EMAIL_REGEX.matches(s.email) -> MSG_EMAIL_INVALIDO
                    else                          -> null
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
                    s.cep.isBlank()   -> MSG_CAMPO_OBRIGATORIO
                    s.cep.length < 8  -> "CEP incompleto — ${s.cep.length}/8 dígitos"
                    else              -> null
                }
                val erroEndereco = if (s.endereco.isBlank()) MSG_CAMPO_OBRIGATORIO else null
                val erroNumber   = if (s.number.isBlank())   MSG_CAMPO_OBRIGATORIO else null
                val erroBairro   = if (s.bairro.isBlank())   MSG_CAMPO_OBRIGATORIO else null
                val erroCidade   = if (s.cidade.isBlank())   MSG_CAMPO_OBRIGATORIO else null
                val erroEstado   = when {
                    s.estado.isBlank()  -> MSG_CAMPO_OBRIGATORIO
                    s.estado.length < 2 -> "Sigla incompleta — informe a UF (ex: SP)"
                    else                -> null
                }
                val valido = erroCep == null && erroEndereco == null && erroNumber == null &&
                             erroBairro == null && erroCidade == null && erroEstado == null
                if (!valido) _uiState.update {
                    it.copy(
                        erroCep      = erroCep,
                        erroEndereco = erroEndereco,
                        erroNumber   = erroNumber,
                        erroBairro   = erroBairro,
                        erroCidade   = erroCidade,
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

    private fun submeterCadastro() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            when {
                route.modoEdicao          -> submeterEdicao()
                route.userId.isNotEmpty() -> submeterFluxoExterno()
                else                      -> submeterFluxoNormal()
            }
        }
    }

    // Fluxo de edição de perfil: usuário já tem cadastro, apenas atualiza os dados.
    private suspend fun submeterEdicao() {
        runCatching {
            val s           = _uiState.value
            val perfilAtual = cadastroUsuarioRepository.buscarPorUserId(route.userId)
                ?: error("Perfil não encontrado")
            cadastroUsuarioRepository.atualizar(
                construirCadastroUsuario(s, route.userId).copy(id = perfilAtual.id)
            )
            _uiEvent.send(CadastroUsuarioUiEvent.NavigateToLista("Dados atualizados com sucesso!"))
        }.onFailure { erro ->
            if (erro is CancellationException) throw erro
            _uiState.update { it.copy(carregando = false) }
            _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro(erro.message ?: "Erro ao salvar"))
        }
    }

    // Fluxo Google / provedor externo: userId já existe, só salva perfil e ativa sessão.
    private suspend fun submeterFluxoExterno() {
        runCatching {
            val s = _uiState.value
            cadastroUsuarioRepository.salvar(construirCadastroUsuario(s, route.userId))
            sessaoRepository.ativarSessao(route.userId)
            _uiEvent.send(CadastroUsuarioUiEvent.NavigateToLista("Cadastro realizado com sucesso!"))
        }.onFailure { erro ->
            if (erro is CancellationException) throw erro
            _uiState.update { it.copy(carregando = false) }
            _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro(erro.message ?: "Erro ao cadastrar"))
        }
    }

    // Fluxo e-mail + senha: cria conta Firebase → salva perfil → ativa sessão.
    // Em caso de falha após a criação Firebase, faz rollback via desfazerCriacaoConta().
    private suspend fun submeterFluxoNormal() {
        var userId: String? = null
        runCatching {
            val s = _uiState.value
            if (cadastroUsuarioRepository.buscarPorEmail(s.email) != null) {
                _uiState.update {
                    it.copy(
                        carregando                = false,
                        erroEmail                 = "E-mail já cadastrado",
                        etapaAtual                = ETAPA_DADOS_PESSOAIS,
                        focarPrimeiroCampoComErro = true,
                    )
                }
                return@runCatching
            }
            val createdUserId = sessaoRepository.criarContaFirebase(s.email.trim(), s.senha)
            userId = createdUserId
            cadastroUsuarioRepository.salvar(construirCadastroUsuario(s, createdUserId))
            sessaoRepository.ativarSessao(createdUserId)
            _uiEvent.send(CadastroUsuarioUiEvent.NavigateToLista("Cadastro realizado com sucesso!"))
        }.onFailure { erro ->
                if (userId != null) sessaoRepository.desfazerCriacaoConta()
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro(erro.message ?: "Erro ao cadastrar"))
            }
    }

    private fun construirCadastroUsuario(s: CadastroUsuarioUiState, userId: String): CadastroUsuario =
        CadastroUsuario(
            userId   = userId,
            nome     = s.nome.trim(),
            cpf      = s.cpf,
            cep      = s.cep,
            endereco = s.endereco.trim(),
            number   = s.number.trim(),
            bairro   = s.bairro.trim(),
            cidade   = s.cidade.trim(),
            estado   = s.estado,
            email    = s.email.trim(),
        )

    private fun buscarCep(cep: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(buscandoCep = true) }
            runCatching {
                val endereco = cadastroUsuarioRepository.buscarEnderecoPorCep(cep)
                if (endereco != null) {
                    _uiState.update {
                        it.copy(
                            endereco     = endereco.logradouro,
                            bairro       = endereco.bairro,
                            cidade       = endereco.cidade,
                            estado       = endereco.estado,
                            erroCep      = null,
                            erroEndereco = null,
                            erroBairro   = null,
                            erroCidade   = null,
                            erroEstado   = null,
                        )
                    }
                } else {
                    _uiState.update { it.copy(erroCep = "CEP não encontrado") }
                }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(erroCep = erro.message ?: "Erro ao buscar CEP") }
            }
            _uiState.update { it.copy(buscandoCep = false) }
        }
    }

    private fun confirmarSenhaErro(senha: String, confirmarSenha: String): String? {
        if (senha.isBlank() || confirmarSenha.isBlank()) return null
        return if (senha != confirmarSenha) "As senhas não coincidem" else null
    }

    // Carrega o perfil do usuário logado para pré-preencher o formulário de edição.
    private fun carregarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            runCatching {
                val perfil = cadastroUsuarioRepository.buscarPorUserId(route.userId)
                    ?: error("Perfil não encontrado")
                _uiState.update {
                    it.copy(
                        carregando   = false,
                        isModoEdicao = true,
                        nome         = perfil.nome,
                        cpf          = perfil.cpf,
                        cep          = perfil.cep,
                        endereco     = perfil.endereco,
                        number       = perfil.number,
                        bairro       = perfil.bairro,
                        cidade       = perfil.cidade,
                        estado       = perfil.estado,
                        email        = perfil.email,
                    )
                }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro(erro.message ?: "Erro ao carregar perfil"))
            }
        }
    }

    // ── Funções auxiliares (movidas de ui/ — usadas apenas neste ViewModel) ────
    private fun formatNumero(text: String): String =
        text.filter { it.isDigit() }.take(8)

    private fun formatUf(text: String): String =
        text.filter { it.isLetter() }.uppercase().take(2)
}

