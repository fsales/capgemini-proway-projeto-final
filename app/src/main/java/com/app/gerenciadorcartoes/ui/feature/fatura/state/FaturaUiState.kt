package com.app.gerenciadorcartoes.ui.feature.fatura.state

data class FaturaUiState(
    val carregando : Boolean             = false,
    val erro       : String?             = null,
    val titulo     : String              = "Faturas",
    val faturas    : List<FaturaMesUiState> = emptyList(),
)

data class FaturaMesUiState(
    val referencia  : String                 = "",
    val lancamentos : List<LancamentoUiState> = emptyList(),
) {
    val total: Double
        get() = lancamentos.sumOf { it.valor }
}

data class LancamentoUiState(
    val descricao : String = "",
    val data      : String = "",
    val valor     : Double = 0.0,
)