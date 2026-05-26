package com.app.gerenciadorcartoes.ui.feature.cadastrousuario

sealed interface CadastroUsuarioUiEvent {
    data object NavigateBack                                   : CadastroUsuarioUiEvent
    /** Navega para Lista exibindo [mensagem] via Snackbar — vazio = sem snackbar. */
    data class  NavigateToLista(val mensagem: String = "")     : CadastroUsuarioUiEvent
    data class  MostrarErro(val mensagem: String)              : CadastroUsuarioUiEvent
    data class  MostrarMensagem(val mensagem: String)          : CadastroUsuarioUiEvent
}

