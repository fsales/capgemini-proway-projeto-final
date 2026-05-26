package com.app.gerenciadorcartoes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gerenciadorcartoes.model.ResultadoAutenticacaoExterna
import com.app.gerenciadorcartoes.repository.SessaoRepository
import com.app.gerenciadorcartoes.ui.feature.splash.SplashUiEvent
import com.app.gerenciadorcartoes.ui.feature.splash.state.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessaoRepository : SessaoRepository,
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
                val autenticado = sessaoRepository.verificarSessaoInicial()
                _uiState.update { it.copy(carregando = false) }
                if (autenticado) {
                    _uiEvent.send(SplashUiEvent.NavigateToLista)
                    return@runCatching
                }
                when (val perfil = sessaoRepository.verificarPerfilGoogleIncompleto()) {
                    ResultadoAutenticacaoExterna.Autenticado ->
                        _uiEvent.send(SplashUiEvent.NavigateToLista)

                    is ResultadoAutenticacaoExterna.PrecisaCadastro ->
                        _uiEvent.send(
                            SplashUiEvent.NavigateToCadastroIncompleto(
                                userId = perfil.userId,
                                email  = perfil.email,
                                nome   = perfil.nome,
                            )
                        )

                    null -> _uiEvent.send(SplashUiEvent.NavigateToLogin)
                }
            }.onFailure { erro ->
                Log.e("SplashViewModel", "Erro ao verificar sessão — redirecionando para Login", erro)
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(SplashUiEvent.NavigateToLogin)
            }
        }
    }
}
