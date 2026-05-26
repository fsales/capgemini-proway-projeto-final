package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.repository.SessaoRepository
import com.app.gerenciadorcartoes.ui.feature.recuperarsenha.RecuperarSenhaEvent
import com.app.gerenciadorcartoes.ui.feature.recuperarsenha.RecuperarSenhaUiEvent
import com.app.gerenciadorcartoes.ui.feature.recuperarsenha.state.RecuperarSenhaUiState
import com.app.gerenciadorcartoes.ui.navigation.RecuperarSenhaRoute
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
class RecuperarSenhaViewModel @Inject constructor(
    savedStateHandle             : SavedStateHandle,
    private val sessaoRepository : SessaoRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RecuperarSenhaRoute>()

    private val _uiState = MutableStateFlow(
        RecuperarSenhaUiState(email = route.emailInicial)
    )
    val uiState: StateFlow<RecuperarSenhaUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<RecuperarSenhaUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: RecuperarSenhaEvent) {
        when (event) {
            is RecuperarSenhaEvent.EmailAlterado ->
                _uiState.update { it.copy(email = event.valor.trim(), erroEmail = null) }

            RecuperarSenhaEvent.Enviar  -> enviar()

            RecuperarSenhaEvent.Voltar  ->
                viewModelScope.launch { _uiEvent.send(RecuperarSenhaUiEvent.NavigateBack) }
        }
    }

    private fun enviar() {
        val email = _uiState.value.email
        val erroEmail = when {
            email.isBlank()              -> "Informe o e-mail"
            !EMAIL_REGEX.matches(email)  -> "E-mail inválido"
            else                         -> null
        }
        if (erroEmail != null) {
            _uiState.update { it.copy(erroEmail = erroEmail) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true) }
            runCatching {
                sessaoRepository.enviarRecuperacaoSenha(email)
                _uiState.update { it.copy(enviando = false) }
                _uiEvent.send(RecuperarSenhaUiEvent.MostrarMensagem("Se este e-mail estiver cadastrado, você receberá as instruções em breve"))
            }.onFailure { erro ->
                _uiState.update { it.copy(enviando = false) }
                _uiEvent.send(RecuperarSenhaUiEvent.MostrarErro(erro.message ?: "Erro ao enviar e-mail"))
            }
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
