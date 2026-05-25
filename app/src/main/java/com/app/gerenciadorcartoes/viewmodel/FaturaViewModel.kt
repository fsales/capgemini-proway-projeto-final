package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.ui.feature.fatura.FaturaEvent
import com.app.gerenciadorcartoes.ui.feature.fatura.FaturaUiEvent
import com.app.gerenciadorcartoes.ui.feature.fatura.state.FaturaMesUiState
import com.app.gerenciadorcartoes.ui.feature.fatura.state.FaturaUiState
import com.app.gerenciadorcartoes.ui.feature.fatura.state.LancamentoUiState
import com.app.gerenciadorcartoes.ui.navigation.FaturaRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.Month
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
                            faturas    = gerarFaturasMock(),
                        )
                    }
                }
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false, erro = erro.message) }
                _uiEvent.send(FaturaUiEvent.MostrarErro(erro.message ?: "Erro ao carregar faturas"))
            }
        }
    }

    private fun gerarFaturasMock(): List<FaturaMesUiState> {
        val anoAtual = LocalDate.now().year
        val inicio = YearMonth.of(anoAtual, Month.MAY)
        val fim = YearMonth.of(2025, Month.NOVEMBER)

        val meses = mutableListOf<YearMonth>()
        var cursor = inicio
        while (!cursor.isBefore(fim)) {
            meses += cursor
            cursor = cursor.minusMonths(1)
        }

        return meses.mapIndexed { indiceFatura, competencia ->
            FaturaMesUiState(
                referencia  = competencia.formatarReferencia(),
                lancamentos = gerarLancamentos(competencia, indiceFatura),
            )
        }
    }

    private fun gerarLancamentos(competencia: YearMonth, indiceFatura: Int): List<LancamentoUiState> =
        (1..5).map { indiceLancamento ->
            val dia = (indiceLancamento * 5 + indiceFatura).coerceAtMost(28)
            val valor = 25.0 + (indiceFatura * 11.75) + (indiceLancamento * 13.40)

            LancamentoUiState(
                descricao = DESCRICOES[(indiceFatura * 5 + indiceLancamento - 1) % DESCRICOES.size],
                data      = "%02d/%02d/%d".format(dia, competencia.monthValue, competencia.year),
                valor     = valor,
            )
        }

    private fun YearMonth.formatarReferencia(): String {
        val nomeMes = month.getDisplayName(TextStyle.FULL, PT_BR)
        return "${nomeMes.replaceFirstChar { it.titlecase(PT_BR) }}/${year}"
    }

    private companion object {
        private val PT_BR = Locale.forLanguageTag("pt-BR")

        private val DESCRICOES = listOf(
            "Academia",
            "Assinatura streaming",
            "Cinema",
            "Combustivel",
            "Delivery",
            "Farmacia",
            "Loja online",
            "Restaurante",
            "Supermercado",
            "Transporte",
        )
    }
}