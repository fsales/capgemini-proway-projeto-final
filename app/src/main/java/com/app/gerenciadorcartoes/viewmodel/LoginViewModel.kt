package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gerenciadorcartoes.ui.feature.login.LoginEvent
import com.app.gerenciadorcartoes.ui.feature.login.LoginUiEvent
import com.app.gerenciadorcartoes.ui.feature.login.state.LoginUiState
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
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<LoginUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Usuario ->
                _uiState.update { it.copy(usuario = event.valor, erroUsuario = null) }

            is LoginEvent.Senha ->
                _uiState.update { it.copy(senha = event.valor, erroSenha = null) }

            LoginEvent.Entrar -> entrar()

            LoginEvent.NavegaParaCadastro ->
                viewModelScope.launch { _uiEvent.send(LoginUiEvent.NavegaParaCadastro) }
        }
    }

    private fun entrar() {
        if (!validar()) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            runCatching {
                verificarCredenciais(
                    usuario = _uiState.value.usuario,
                    senha   = _uiState.value.senha,
                )
                _uiEvent.send(LoginUiEvent.NavegaParaLista)
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(
                    LoginUiEvent.MostrarErro(erro.message ?: "Usuário ou senha incorretos")
                )
            }
        }
    }

    /** Valida todos os campos de uma vez — nunca aborta no primeiro erro. */
    private fun validar(): Boolean {
        var valido = true
        val s = _uiState.value

        if (s.usuario.isBlank()) {
            _uiState.update { it.copy(erroUsuario = "Informe o usuário") }
            valido = false
        }
        if (s.senha.isBlank()) {
            _uiState.update { it.copy(erroSenha = "Informe a senha") }
            valido = false
        }
        return valido
    }

    /**
     * Simulação local de autenticação.
     * Substitua pelo repositório de autenticação real quando disponível.
     */
    @Throws(Exception::class)
    private fun verificarCredenciais(usuario: String, senha: String) {
        if (usuario != "gustavo" || senha != "1234") {
            throw Exception("Usuário ou senha incorretos")
        }
    }
}
