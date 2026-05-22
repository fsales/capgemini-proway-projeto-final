package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gerenciadorcartoes.data.local.session.SessionManager
import com.app.gerenciadorcartoes.ui.feature.splash.SplashUiEvent
import com.app.gerenciadorcartoes.ui.feature.splash.state.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<SplashUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        verificarSessao()
    }

    private fun verificarSessao() {
        viewModelScope.launch {
            runCatching {
                delay(2_200L)
                val logado = sessionManager.isLoggedIn().first()
                _uiState.update { it.copy(carregando = false) }
                if (logado) {
                    _uiEvent.send(SplashUiEvent.NavigateToLista)
                } else {
                    _uiEvent.send(SplashUiEvent.NavigateToLogin)
                }
            }.onFailure {
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(SplashUiEvent.NavigateToLogin)
            }
        }
    }
}


