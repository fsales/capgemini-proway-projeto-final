package com.app.gerenciadorcartoes.model

import java.time.LocalDate
import java.time.YearMonth

data class FaturaMes(
    val competencia : YearMonth = YearMonth.of(1970, 1),
    val lancamentos : List<LancamentoFatura> = emptyList(),
) {
    val total: Double
        get() = lancamentos.sumOf { it.valor }
}

data class LancamentoFatura(
    val descricao : String = "",
    val data      : LocalDate = LocalDate.of(1970, 1, 1),
    val valor     : Double = 0.0,
)
