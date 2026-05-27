package com.app.gerenciadorcartoes.ui.feature.fatura

sealed interface FaturaEvent {
    data object Voltar : FaturaEvent
}