package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.repository.SessaoRepository
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.CadastrarAlterarEvent
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.CadastrarAlterarUiEvent
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.state.CadastrarAlterarUiState
import com.app.gerenciadorcartoes.ui.navigation.CadastrarAlterarRoute
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
class CadastrarAlterarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cartaoRepository: CartaoRepository,
    private val sessaoRepository          : SessaoRepository,
    private val syncCoordinator          : com.app.gerenciadorcartoes.sync.SyncCoordinator,
) : ViewModel() {

    private val route: CadastrarAlterarRoute = savedStateHandle.toRoute()
    private val id: Long = route.id

    private val _uiState = MutableStateFlow(CadastrarAlterarUiState(carregando = id != 0L, modoEdicao = id != 0L))
    val uiState: StateFlow<CadastrarAlterarUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<CadastrarAlterarUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        if (id != 0L) carregarCartao()
    }

    fun onEvent(event: CadastrarAlterarEvent) {
        when (event) {
            CadastrarAlterarEvent.Voltar ->
                viewModelScope.launch { _uiEvent.send(CadastrarAlterarUiEvent.NavigateBack) }

            CadastrarAlterarEvent.Salvar -> salvar()

            is CadastrarAlterarEvent.NomeTitularAlterado ->
                _uiState.update { it.copy(nomeTitular = event.valor, erroNome = null) }

            is CadastrarAlterarEvent.FinalNumeroAlterado ->
                _uiState.update { it.copy(finalNumero = event.valor.take(4), erroNumero = null) }

            is CadastrarAlterarEvent.BandeiraAlterada ->
                _uiState.update { it.copy(bandeira = event.valor, erroBandeira = null) }

            is CadastrarAlterarEvent.ValidadeAlterada ->
                _uiState.update { it.copy(validade = event.valor, erroValidade = null) }

            is CadastrarAlterarEvent.LimiteAlterado ->
                _uiState.update { it.copy(limite = event.valor, erroLimite = null) }

            is CadastrarAlterarEvent.TemplateAlterado ->
                _uiState.update { it.copy(template = event.valor) }
        }
    }

    // -------------------------------------------------------------------------
    // Privado
    // -------------------------------------------------------------------------

    private fun carregarCartao() {
        viewModelScope.launch {
            runCatching {
                val cartao = cartaoRepository.buscarPorId(id)
                if (cartao != null) {
                    _uiState.update {
                        it.copy(
                            nomeTitular = cartao.nomeTitular,
                            finalNumero = cartao.finalNumero,
                            bandeira    = cartao.bandeira,
                            validade    = cartao.validade.filter { it.isDigit() },
                            limite      = cartao.limiteMaximo
                                .takeIf { limite -> limite > 0.0 }
                                ?: cartao.limite,
                            template    = cartao.template,
                            carregando  = false,
                        )
                    }
                } else {
                    _uiState.update { it.copy(carregando = false) }
                    _uiEvent.send(CadastrarAlterarUiEvent.NavigateBack)
                }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(
                    CadastrarAlterarUiEvent.MostrarErro(
                            erro.message ?: "Erro ao carregar cartão"
                    )
                )
            }
        }
    }

    private fun salvar() {
        if (!validar()) return

        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true) }
            runCatching {
                val s            = _uiState.value
                val limiteMaximo = s.limite
                val limiteAtual  = if (id == 0L) {
                    limiteMaximo
                } else {
                    cartaoRepository.buscarPorId(id)
                        ?.limite
                        ?.coerceAtMost(limiteMaximo)
                        ?: limiteMaximo
                }
                val idUsuario = sessaoRepository.buscarIdUsuario()
                    ?: error("Sessão local não encontrada. Faça login novamente.")
                val cartao = Cartao.fromUi(
                    id          = id,
                    nomeTitular = s.nomeTitular,
                    finalNumero = s.finalNumero,
                    bandeira    = s.bandeira,
                    validadeRaw = s.validade,
                    limite      = limiteAtual,
                    limiteMaximo= limiteMaximo,
                    template    = s.template,
                    usuarioId = idUsuario
                )
                if (id == 0L){
                    cartaoRepository.salvar(cartao)
                    // Solicita a sincronização com a API remota via SyncCoordinator
                    syncCoordinator.scheduleSync()
                }
                else cartaoRepository.atualizar(cartao)
                _uiEvent.send(CadastrarAlterarUiEvent.NavigateBack)
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(salvando = false) }
                _uiEvent.send(
                    CadastrarAlterarUiEvent.MostrarErro(
                        erro.message ?: "Erro ao salvar cartão"
                    )
                )
            }
        }
    }

    private fun validar(): Boolean {
        val s = _uiState.value
        var valid = true

        fun setErro(update: CadastrarAlterarUiState.() -> CadastrarAlterarUiState) {
            _uiState.update(update)
            valid = false
        }

        with(s) {
            if (nomeTitular.isBlank()) setErro { copy(erroNome = "Nome é obrigatório") }

            if (finalNumero.length != 4 || !finalNumero.all { it.isDigit() }) {
                setErro { copy(erroNumero = "Informe os 4 últimos dígitos") }
            }

            if (bandeira.isBlank()) setErro { copy(erroBandeira = "Bandeira é obrigatória") }

            validade.let { v ->
                val mes = v.take(2).toIntOrNull() ?: 0
                val ano = v.takeLast(2).toIntOrNull() ?: 0
                val anoAtual = java.time.LocalDate.now().year % 100

                when {
                    v.length != 4 -> setErro { copy(erroValidade = "Informe MM/AA") }
                    mes !in 1..12 -> setErro { copy(erroValidade = "Mês inválido (01-12)") }
                    ano < anoAtual -> setErro { copy(erroValidade = "Ano deve ser atual ou futuro") }
                }
            }

            if (limite <= 0.0) setErro { copy(erroLimite = "Limite inválido") }
        }

        return valid
    }


    // Chamadas remotas delegadas ao repositório; Erros de rede não propagam para o fluxo principal
}


