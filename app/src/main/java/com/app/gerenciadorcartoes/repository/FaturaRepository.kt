package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.FaturaMes

interface FaturaRepository {
    fun listarFaturas(cartaoId: Long): List<FaturaMes>
    fun totalAPartirDoMesAtual(cartaoId: Long): Double
}
