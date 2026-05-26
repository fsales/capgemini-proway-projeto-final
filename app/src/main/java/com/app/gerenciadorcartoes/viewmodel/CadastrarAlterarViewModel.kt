package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.data.local.session.SessionManager
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.network.model.AddCardRequest
import com.app.gerenciadorcartoes.network.service.ApiService
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.CadastrarAlterarEvent
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.CadastrarAlterarUiEvent
import com.app.gerenciadorcartoes.ui.feature.cadastraralterar.state.CadastrarAlterarUiState
import com.app.gerenciadorcartoes.ui.navigation.CadastrarAlterarRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
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
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
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
                            validade    = cartao.validade,
                            limite      = cartao.limiteMaximo
                                .takeIf { limite -> limite > 0.0 }
                                ?.toString()
                                ?: cartao.limite.toString(),
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
                val limiteMaximo = s.limite.toDoubleOrNull() ?: 0.0
                val limiteAtual  = if (id == 0L) {
                    limiteMaximo
                } else {
                    cartaoRepository.buscarPorId(id)
                        ?.limite
                        ?.coerceAtMost(limiteMaximo)
                        ?: limiteMaximo
                }
                val cartao = Cartao(
                    id          = id,
                    nomeTitular = s.nomeTitular.trim(),
                    finalNumero = s.finalNumero.trim(),
                    bandeira    = s.bandeira.trim(),
                    validade    = s.validade.trim(),
                    limite      = limiteAtual,
                    limiteMaximo= limiteMaximo,
                    template    = s.template,
                )
                if (id == 0L){
                    cartaoRepository.salvar(cartao)
                    enviarCartaoParaApi(cartao)
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

        if (s.nomeTitular.isBlank()) {
            _uiState.update { it.copy(erroNome = "Nome é obrigatório") }; valid = false
        }
        if (s.finalNumero.length != 4 || !s.finalNumero.all { it.isDigit() }) {
            _uiState.update { it.copy(erroNumero = "Informe os 4 últimos dígitos") }; valid = false
        }
        if (s.bandeira.isBlank()) {
            _uiState.update { it.copy(erroBandeira = "Bandeira é obrigatória") }; valid = false
        }
        if (!Regex("""^\d{2}/\d{2}$""").matches(s.validade)) {
            _uiState.update { it.copy(erroValidade = "Formato MM/AA") }; valid = false
        }
        val limite = s.limite.toDoubleOrNull()
        if (s.limite.isBlank() || limite == null || limite <= 0.0) {
            _uiState.update { it.copy(erroLimite = "Limite inválido") }; valid = false
        }
        return valid
    }


    private suspend fun enviarCartaoParaApi(cartao: Cartao) {
        runCatching {
            val userId = sessionManager.getSessionUserId().firstOrNull() ?: return
            apiService.addCard(
                AddCardRequest(
                    idUsuario   = userId,
                    id = cartao.id.toString(),
                    nomeTitular = cartao.nomeTitular,
                    finalNumero = cartao.finalNumero,
                    bandeira    = cartao.bandeira,
                    validade    = cartao.validade,
                    limite      = cartao.limite,
                    template    = cartao.template,
                    bloqueado   = cartao.bloqueado,
                )
            )
        }
        // Erros de rede não propagam: o save local já foi concluído com sucesso.
    }
}

