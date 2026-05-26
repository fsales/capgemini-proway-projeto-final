package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.ui.feature.ajustarlimite.AjustarLimiteEvent
import com.app.gerenciadorcartoes.ui.feature.ajustarlimite.AjustarLimiteUiEvent
import com.app.gerenciadorcartoes.ui.feature.ajustarlimite.state.AjustarLimiteUiState
import com.app.gerenciadorcartoes.ui.navigation.AjustarLimiteRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class AjustarLimiteViewModel @Inject constructor(
    savedStateHandle             : SavedStateHandle,
    private val cartaoRepository : CartaoRepository,
) : ViewModel() {

    private val route : AjustarLimiteRoute = savedStateHandle.toRoute()
    private val id    : Long                = route.id

    private val _uiState = MutableStateFlow(AjustarLimiteUiState(carregando = true))
    val uiState: StateFlow<AjustarLimiteUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AjustarLimiteUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observarCartao()
    }

    fun onEvent(event: AjustarLimiteEvent) {
        when (event) {
            AjustarLimiteEvent.Voltar ->
                viewModelScope.launch { _uiEvent.send(AjustarLimiteUiEvent.NavigateBack) }

            AjustarLimiteEvent.Salvar -> validarParaConfirmacao()

            AjustarLimiteEvent.ConfirmarSalvar -> salvarLimite()

            AjustarLimiteEvent.CancelarConfirmacao ->
                _uiState.update { it.copy(mostrarConfirmacao = false) }

            is AjustarLimiteEvent.NovoLimiteAlterado ->
                _uiState.update {
                    val limiteMaximo = limiteMaximoValido(it.limiteMaximo)
                    it.copy(
                        novoLimite = event.valor.coerceIn(LIMITE_MINIMO, limiteMaximo),
                        erroLimite = null,
                        mensagem   = null,
                    )
                }
        }
    }

    private fun observarCartao() {
        viewModelScope.launch {
            runCatching {
                cartaoRepository.observarPorId(id).collect { cartao ->
                    if (cartao == null) {
                        _uiState.update {
                            it.copy(
                                limiteAtual      = LIMITE_PADRAO,
                                limiteMaximo     = LIMITE_PADRAO,
                                novoLimite       = it.novoLimite.takeIf { limite -> limite > 0.0 } ?: LIMITE_PADRAO,
                                cartaoEncontrado = false,
                                carregando       = false,
                                salvando         = false,
                                aviso            = "Nenhum cartão foi encontrado para salvar o ajuste.",
                            )
                        }
                    } else {
                        val limiteMaximo = limiteMaximoValido(
                            cartao.limiteMaximo.takeIf { it > 0.0 } ?: cartao.limite
                        )
                        val limiteAtual = cartao.limite
                            .takeIf { it > 0.0 }
                            ?.coerceAtMost(limiteMaximo)
                            ?: limiteMaximo
                        _uiState.update { state ->
                            val novoLimite = state.novoLimite
                                .takeIf { it > 0.0 }
                                ?: limiteAtual
                            state.copy(
                                limiteAtual       = limiteAtual,
                                limiteMaximo      = limiteMaximo,
                                novoLimite        = novoLimite.coerceIn(LIMITE_MINIMO, limiteMaximo),
                                cartaoFinalNumero = cartao.finalNumero,
                                cartaoEncontrado  = true,
                                carregando        = false,
                                aviso             = null,
                            )
                        }
                    }
                }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(
                    AjustarLimiteUiEvent.MostrarMensagem(
                        erro.message ?: "Erro ao carregar limite do cartão"
                    )
                )
            }
        }
    }

    private fun validarParaConfirmacao() {
        val state      = _uiState.value
        val novoLimite = state.novoLimite
        val limiteMaximo = limiteMaximoValido(state.limiteMaximo)

        val erro = when {
            !state.cartaoEncontrado           -> "Não há cartão disponível para salvar este limite."
            novoLimite <= 0.0                -> "O limite deve ser maior que zero."
            novoLimite > limiteMaximo        -> "O limite máximo permitido é ${formatarMoeda(limiteMaximo)}."
            abs(novoLimite - state.limiteAtual) < 0.01 -> "O novo limite deve ser diferente do atual."
            else                             -> null
        }

        if (erro != null) {
            _uiState.update {
                it.copy(
                    erroLimite         = erro,
                    mostrarConfirmacao = false,
                    mensagem           = null,
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                erroLimite         = null,
                mostrarConfirmacao = true,
                limiteConfirmacao  = novoLimite,
                mensagem           = null,
            )
        }
    }

    private fun salvarLimite() {
        val novoLimite = _uiState.value.limiteConfirmacao

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    salvando           = true,
                    mostrarConfirmacao = false,
                    mensagem           = null,
                )
            }
            runCatching {
                cartaoRepository.atualizarLimite(id, novoLimite)
                _uiState.update {
                    it.copy(
                        novoLimite = novoLimite,
                        salvando   = false,
                        mensagem   = "Limite atualizado com sucesso.",
                    )
                }
                _uiEvent.send(AjustarLimiteUiEvent.NavigateBack)
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update {
                    it.copy(
                        salvando   = false,
                        erroLimite = erro.message ?: "Erro ao salvar limite.",
                    )
                }
                _uiEvent.send(
                    AjustarLimiteUiEvent.MostrarMensagem(
                        erro.message ?: "Erro ao salvar limite."
                    )
                )
            }
        }
    }

    private fun limiteMaximoValido(limiteMaximo: Double): Double =
        maxOf(LIMITE_MINIMO, limiteMaximo.takeIf { it > 0.0 } ?: LIMITE_PADRAO)

    private fun formatarMoeda(valor: Double): String =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor)

    private companion object {
        const val LIMITE_MINIMO = 1.0
        const val LIMITE_PADRAO = 1_000.0
    }
}
