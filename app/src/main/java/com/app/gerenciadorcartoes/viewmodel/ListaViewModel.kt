package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.ui.feature.lista.ListaEvent
import com.app.gerenciadorcartoes.ui.feature.lista.ListaUiEvent
import com.app.gerenciadorcartoes.ui.feature.lista.state.ListaUiState
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
class ListaViewModel @Inject constructor(
    private val cartaoRepository: CartaoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaUiState(carregando = true))

    val uiState: StateFlow<ListaUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<ListaUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observarCartoes()
    }

    fun onEvent(event: ListaEvent) {
        when (event) {
            is ListaEvent.NavegaParaItem ->
                viewModelScope.launch { _uiEvent.send(ListaUiEvent.NavegaParaItem(event.id)) }

            is ListaEvent.NavegaParaEditar ->
                viewModelScope.launch { _uiEvent.send(ListaUiEvent.NavegaParaEditar(event.id)) }

            ListaEvent.NavegaParaNovo ->
                viewModelScope.launch { _uiEvent.send(ListaUiEvent.NavegaParaNovo) }

            is ListaEvent.ExcluirCartao -> excluir(event.id)

            ListaEvent.Deslogar ->
                viewModelScope.launch { _uiEvent.send(ListaUiEvent.NavegaParaLogin) }
        }
    }

    private fun observarCartoes() {
        viewModelScope.launch {
            runCatching {
                cartaoRepository.observarTodos().collect { cartoes ->
                    _uiState.update { it.copy(cartoes = cartoes, carregando = false) }
                }
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false, erro = erro.message) }
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao carregar cartões"))
            }
        }
    }

    private fun excluir(id: Long) {
        viewModelScope.launch {
            runCatching {
                cartaoRepository.excluirPorId(id)
                _uiEvent.send(ListaUiEvent.MostrarMensagem("Cartão removido com sucesso"))
            }.onFailure { erro ->
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao remover cartão"))
            }
        }
    }
}
