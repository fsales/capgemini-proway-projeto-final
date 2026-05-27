package com.app.gerenciadorcartoes.ui.feature.detalhe

sealed interface DetalheEvent {
    data object Voltar         : DetalheEvent
    data object AjustarLimite  : DetalheEvent
    data object VerFaturas     : DetalheEvent
    data object BloquearCartao : DetalheEvent
}
