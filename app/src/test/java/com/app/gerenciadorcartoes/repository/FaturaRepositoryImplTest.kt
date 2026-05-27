package com.app.gerenciadorcartoes.repository

import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class FaturaRepositoryImplTest {

    private val clock = Clock.fixed(
        Instant.parse("2026-05-27T12:00:00Z"),
        ZoneId.of("America/Manaus"),
    )

    @Test
    fun `listarFaturas should start at current month and move forward`() {
        val repository = FaturaRepositoryImpl(clock)
        val mesAtual = YearMonth.of(2026, 5)

        val faturas = repository.listarFaturas(cartaoId = 1L)

        assertEquals(mesAtual, faturas.first().competencia)
        assertEquals(mesAtual.plusMonths(6), faturas.last().competencia)
    }

    @Test
    fun `totalAPartirDoMesAtual should sum current and future invoices`() {
        val repository = FaturaRepositoryImpl(clock)
        val mesAtual = YearMonth.of(2026, 5)

        val faturas = repository.listarFaturas(cartaoId = 1L)
        val esperado = faturas
            .filter { !it.competencia.isBefore(mesAtual) }
            .sumOf { it.total }

        assertEquals(3_515.75, esperado, 0.001)
        assertEquals(esperado, repository.totalAPartirDoMesAtual(cartaoId = 1L), 0.001)
    }
}
