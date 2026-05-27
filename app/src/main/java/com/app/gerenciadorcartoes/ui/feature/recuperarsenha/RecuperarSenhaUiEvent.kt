package com.app.gerenciadorcartoes.ui.feature.recuperarsenha

sealed interface RecuperarSenhaUiEvent {
    data object NavigateBack                          : RecuperarSenhaUiEvent
    data class  MostrarErro(val mensagem: String)     : RecuperarSenhaUiEvent
    data class  MostrarMensagem(val mensagem: String) : RecuperarSenhaUiEvent
}
