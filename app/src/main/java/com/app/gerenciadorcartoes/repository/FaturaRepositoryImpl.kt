package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.FaturaMes
import com.app.gerenciadorcartoes.model.LancamentoFatura
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject

class FaturaRepositoryImpl @Inject constructor(
    private val clock: Clock,
) : FaturaRepository {

    override fun listarFaturas(cartaoId: Long): List<FaturaMes> {
        val mesAtual = YearMonth.now(clock)

        return (0 until QUANTIDADE_MESES_MOCK).map { indiceFatura ->
            val competencia = mesAtual.plusMonths(indiceFatura.toLong())

            FaturaMes(
                competencia = competencia,
                lancamentos = gerarLancamentos(competencia, indiceFatura),
            )
        }
    }

    override fun totalAPartirDoMesAtual(cartaoId: Long): Double {
        val mesAtual = YearMonth.now(clock)

        return listarFaturas(cartaoId)
            .filter { !it.competencia.isBefore(mesAtual) }
            .sumOf { it.total }
    }

    private fun gerarLancamentos(competencia: YearMonth, indiceFatura: Int): List<LancamentoFatura> =
        (1..LANCAMENTOS_POR_FATURA).map { indiceLancamento ->
            val dia = (indiceLancamento * 5 + indiceFatura).coerceAtMost(28)
            val valor = 25.0 + (indiceFatura * 11.75) + (indiceLancamento * 13.40)

            LancamentoFatura(
                descricao = DESCRICOES[(indiceFatura * 5 + indiceLancamento - 1) % DESCRICOES.size],
                data      = competencia.atDay(dia),
                valor     = valor,
            )
        }

    private companion object {
        const val QUANTIDADE_MESES_MOCK = 7
        const val LANCAMENTOS_POR_FATURA = 5

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
