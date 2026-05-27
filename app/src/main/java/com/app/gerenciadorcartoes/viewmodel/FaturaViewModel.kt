package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.model.FaturaMes
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.repository.FaturaRepository
import com.app.gerenciadorcartoes.ui.feature.fatura.FaturaEvent
import com.app.gerenciadorcartoes.ui.feature.fatura.FaturaUiEvent
import com.app.gerenciadorcartoes.ui.feature.fatura.state.FaturaMesUiState
import com.app.gerenciadorcartoes.ui.feature.fatura.state.FaturaUiState
import com.app.gerenciadorcartoes.ui.feature.fatura.state.LancamentoUiState
import com.app.gerenciadorcartoes.ui.navigation.FaturaRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FaturaViewModel @Inject constructor(
    savedStateHandle         : SavedStateHandle,
    private val cartaoRepository: CartaoRepository,
    private val faturaRepository: FaturaRepository,
) : ViewModel() {

    private val route : FaturaRoute = savedStateHandle.toRoute()
    private val id    : Long         = route.id

    private val _uiState = MutableStateFlow(FaturaUiState(carregando = true))
    val uiState: StateFlow<FaturaUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<FaturaUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        carregarFaturas()
    }

    fun onEvent(event: FaturaEvent) {
        when (event) {
            FaturaEvent.Voltar -> viewModelScope.launch { _uiEvent.send(FaturaUiEvent.NavigateBack) }
        }
    }

    private fun carregarFaturas() {
        viewModelScope.launch {
            runCatching {
                val cartao = cartaoRepository.buscarPorId(id)
                if (cartao == null) {
                    _uiState.update { it.copy(carregando = false) }
                    _uiEvent.send(FaturaUiEvent.NavigateBack)
                } else {
                    val titulo = if (cartao.finalNumero.isBlank()) {
                        "Faturas"
                    } else {
                        "Faturas •••• ${cartao.finalNumero}"
                    }

                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erro       = null,
                            titulo     = titulo,
                            faturas    = faturaRepository.listarFaturas(id).map { it.toUiState() },
                        )
                    }
                }
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false, erro = erro.message) }
                _uiEvent.send(FaturaUiEvent.MostrarErro(erro.message ?: "Erro ao carregar faturas"))
            }
        }
    }

    private fun FaturaMes.toUiState(): FaturaMesUiState =
        FaturaMesUiState(
            referencia  = competencia.formatarReferencia(),
            lancamentos = lancamentos.map {
                LancamentoUiState(
                    descricao = it.descricao,
                    data      = it.data.formatarData(),
                    valor     = it.valor,
                )
            },
        )

    private fun LocalDate.formatarData(): String =
        "%02d/%02d/%d".format(dayOfMonth, monthValue, year)

    private fun YearMonth.formatarReferencia(): String {
        val nomeMes = month.getDisplayName(TextStyle.FULL, PT_BR)
        return "${nomeMes.replaceFirstChar { it.titlecase(PT_BR) }}/${year}"
    }

    private companion object {
        private val PT_BR = Locale.forLanguageTag("pt-BR")
    }
}
