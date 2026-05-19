package com.app.gerenciadorcartoes.ui.feature.login

sealed interface LoginEvent {
    /** Usuário digitou no campo usuário */
    data class Usuario(val valor: String) : LoginEvent
    /** Usuário digitou no campo senha */
    data class Senha(val valor: String)   : LoginEvent
    /** Botão "Entrar" acionado */
    data object Entrar                             : LoginEvent
    /** Botão "Cadastrar" acionado — navega para cadastro */
    data object NavegaParaCadastro                 : LoginEvent
}

