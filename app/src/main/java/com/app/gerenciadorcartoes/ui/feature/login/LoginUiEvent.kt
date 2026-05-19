package com.app.gerenciadorcartoes.ui.feature.login

sealed interface LoginUiEvent {
    /** Login realizado com sucesso — navega para a tela principal */
    data object NavegaParaLista                        : LoginUiEvent
    /** Ir para tela de cadastro */
    data object NavegaParaCadastro                     : LoginUiEvent
    /** Exibe mensagem de erro via Snackbar */
    data class  MostrarErro(val mensagem: String)      : LoginUiEvent
}

