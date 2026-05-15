package com.app.gerenciadorcartoes.ui.feature.cadastraralterar

sealed interface CadastrarAlterarUiEvent {
    data object NavigateBack                      : CadastrarAlterarUiEvent
    data class  MostrarErro(val mensagem: String) : CadastrarAlterarUiEvent
}

