package com.app.gerenciadorcartoes.ui.feature.detalhe

sealed interface DetalheEvent {
    data object Voltar : DetalheEvent
}
