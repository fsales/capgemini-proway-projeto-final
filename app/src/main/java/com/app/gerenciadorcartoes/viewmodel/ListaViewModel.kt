package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.repository.SessaoRepository
import com.app.gerenciadorcartoes.ui.feature.lista.ListaEvent
import com.app.gerenciadorcartoes.ui.feature.lista.ListaUiEvent
import com.app.gerenciadorcartoes.ui.feature.lista.state.ListaUiState
import com.app.gerenciadorcartoes.ui.navigation.ListaRoute
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
class ListaViewModel @Inject constructor(
    savedStateHandle             : SavedStateHandle,
    private val cartaoRepository : CartaoRepository,
    private val sessaoRepository : SessaoRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ListaRoute>()

    private val _uiState = MutableStateFlow(ListaUiState(carregando = true))

    val uiState: StateFlow<ListaUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<ListaUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observarCartoes()
        observarDesconexao()
        carregarUsuario()
        if (route.exibirConfirmacao) {
            viewModelScope.launch {
                _uiEvent.send(ListaUiEvent.MostrarMensagem("Cadastro realizado com sucesso!"))
            }
        }
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

            ListaEvent.Deslogar -> deslogar()

            ListaEvent.NavegaParaPerfil -> navegarParaPerfil()
        }
    }

    private fun carregarUsuario() {
        viewModelScope.launch {
            runCatching {
                val userId = sessaoRepository.buscarUserId()
                val nome   = sessaoRepository.buscarNomeUsuario()
                _uiState.update { it.copy(nomeUsuario = nome, userId = userId) }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao carregar usuário"))
            }
        }
    }

    private fun navegarParaPerfil() {
        viewModelScope.launch {
            runCatching {
                val userId = _uiState.value.userId
                    ?: sessaoRepository.buscarUserId()
                    ?: error("Sessão não encontrada")
                _uiEvent.send(ListaUiEvent.NavegaParaPerfil(userId))
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao abrir perfil"))
            }
        }
    }

    private fun observarCartoes() {
        viewModelScope.launch {
            runCatching {
                val idUsuario = sessaoRepository.buscarIdUsuario()
                cartaoRepository.buscarCartaosPorUsuario(idUsuario).collect { cartoes ->
                    _uiState.update { it.copy(cartoes = cartoes, carregando = false) }
                }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiState.update { it.copy(carregando = false, erro = erro.message) }
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao carregar cartões"))
            }
        }
    }

    private fun observarDesconexao() {
        viewModelScope.launch {
            runCatching {
                sessaoRepository.observarDesconexaoExterna().collect {
                    _uiEvent.send(ListaUiEvent.NavegaParaLogin)
                }
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao monitorar sessão"))
            }
        }
    }

    private fun excluir(id: Long) {
        viewModelScope.launch {
            runCatching {
                cartaoRepository.excluirPorId(id)
                _uiEvent.send(ListaUiEvent.MostrarMensagem("Cartão removido com sucesso"))
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao remover cartão"))
            }
        }
    }

    private fun deslogar() {
        viewModelScope.launch {
            runCatching {
                sessaoRepository.encerrarSessao()
                _uiEvent.send(ListaUiEvent.NavegaParaLogin)
            }.onFailure { erro ->
                if (erro is CancellationException) throw erro
                _uiEvent.send(ListaUiEvent.MostrarErro(erro.message ?: "Erro ao encerrar sessão"))
            }
        }
    }
}
