package com.app.gerenciadorcartoes.ui.feature.detalhe

sealed interface DetalheUiEvent {
    data object NavigateBack                          : DetalheUiEvent
    data class  NavigateToAjustarLimite(val id: Long) : DetalheUiEvent
    data class  NavigateToFatura(val id: Long)        : DetalheUiEvent
    data class  MostrarErro(val mensagem: String)     : DetalheUiEvent
}