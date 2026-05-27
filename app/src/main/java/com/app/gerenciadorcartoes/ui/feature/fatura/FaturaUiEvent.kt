package com.app.gerenciadorcartoes.ui.feature.fatura

sealed interface FaturaUiEvent {
    data object NavigateBack                      : FaturaUiEvent
    data class  MostrarErro(val mensagem: String) : FaturaUiEvent
}