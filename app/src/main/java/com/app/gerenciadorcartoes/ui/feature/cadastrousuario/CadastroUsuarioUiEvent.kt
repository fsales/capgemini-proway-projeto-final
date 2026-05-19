package com.app.gerenciadorcartoes.ui.feature.cadastrousuario

sealed interface CadastroUsuarioUiEvent {
    data object NavigateBack                        : CadastroUsuarioUiEvent

    data class  MostrarErro(val mensagem: String)   : CadastroUsuarioUiEvent
    data class  MostrarMensagem(val mensagem: String) : CadastroUsuarioUiEvent
}

