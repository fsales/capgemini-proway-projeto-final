package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.data.local.session.SessionManager
import com.app.gerenciadorcartoes.network.model.BlockCardRequest
import com.app.gerenciadorcartoes.network.service.ApiService
import com.app.gerenciadorcartoes.repository.CartaoDetalheRepository
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.ui.feature.detalhe.DetalheEvent
import com.app.gerenciadorcartoes.ui.feature.detalhe.DetalheUiEvent
import com.app.gerenciadorcartoes.ui.feature.detalhe.state.DetalheUiState
import com.app.gerenciadorcartoes.ui.navigation.DetalheRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalheViewModel @Inject constructor(
    savedStateHandle              : SavedStateHandle,
    private val detalheRepository : CartaoDetalheRepository,
    private val cartaoRepository : CartaoRepository,
    private val sessionManager    : SessionManager,
    private val apiService        : ApiService,
) : ViewModel() {

    private val route : DetalheRoute = savedStateHandle.toRoute()
    private val id    : Long          = route.id

    private val _uiState = MutableStateFlow(DetalheUiState(carregando = true))
    val uiState: StateFlow<DetalheUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<DetalheUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observarCartao()
    }

    fun onEvent(event: DetalheEvent) {
        when (event) {
            DetalheEvent.Voltar -> viewModelScope.launch { _uiEvent.send(DetalheUiEvent.NavigateBack) }
            DetalheEvent.AjustarLimite -> viewModelScope.launch {
                _uiEvent.send(DetalheUiEvent.NavigateToAjustarLimite(id))
            }
            DetalheEvent.BloquearCartao -> bloquearCartao()
        }
    }

    // Usa Flow reativo: qualquer atualização no banco (inclusive após edição)
    // reenvia automaticamente o estado atualizado para a UI.
    private fun observarCartao() {
        viewModelScope.launch {
            runCatching {
                detalheRepository.observarDetalhePorId(id).collect { detalhe ->
                    if (detalhe != null) {
                        _uiState.update { it.copy(detalhe = detalhe, carregando = false) }
                    } else {
                        // Cartão excluído externamente — navega de volta
                        _uiState.update { it.copy(carregando = false) }
                        _uiEvent.send(DetalheUiEvent.NavigateBack)
                    }
                }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(carregando = false, erro = erro.message) }
                _uiEvent.send(DetalheUiEvent.MostrarErro(erro.message ?: "Erro ao carregar cartão"))
            }
        }
    }

    private fun bloquearCartao() {
        viewModelScope.launch {
            runCatching {
                val cartao = _uiState.value.detalhe.cartao
                val novoStatusBloqueio = !cartao.bloqueado

                // Chamar API para bloquear ou desbloquear

                val userId = sessionManager.getSessionUserId().firstOrNull() ?: throw Exception("Usuário não autenticado")
                if (novoStatusBloqueio) {
                    apiService.blockCard(BlockCardRequest(userId,cartao.id.toString()))
                } else {
                    apiService.unblockCard(BlockCardRequest(userId, cartao.id.toString()))
                }

                // Atualizar no banco de dados local
                cartaoRepository.atualizarBloqueio(cartao.id, novoStatusBloqueio)

                // Enviar mensagem de sucesso
                val mensagem = if (novoStatusBloqueio) {
                    "Cartão bloqueado com sucesso"
                } else {
                    "Cartão desbloqueado com sucesso"
                }
                _uiEvent.send(DetalheUiEvent.MostrarMensagem(mensagem))
            }.onFailure { erro ->
                _uiEvent.send(DetalheUiEvent.MostrarErro(erro.message ?: "Erro ao bloquear cartão"))
            }
        }
    }

}
