package com.app.gerenciadorcartoes.ui.feature.login

sealed interface LoginUiEvent {
    data object NavegaParaLista                        : LoginUiEvent
    data object NavegaParaCadastro                     : LoginUiEvent
    data class  MostrarErro(val mensagem: String)      : LoginUiEvent
    data class  NavegaParaCadastroExterno(
        val userId : String,
        val email  : String,
        val nome   : String,
    ) : LoginUiEvent
    data class  MostrarMensagem(val mensagem: String)       : LoginUiEvent
    data class  NavegaParaRecuperarSenha(val email: String) : LoginUiEvent
}
