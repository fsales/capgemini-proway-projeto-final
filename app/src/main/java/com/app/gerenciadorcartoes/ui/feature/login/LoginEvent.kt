package com.app.gerenciadorcartoes.ui.feature.login

sealed interface LoginEvent {
    data class  Usuario(val valor: String)                    : LoginEvent
    data class  Senha(val valor: String)                      : LoginEvent
    data object Entrar                                        : LoginEvent
    data object NavegaParaCadastro                            : LoginEvent
    data class  EntrarComProvedorExterno(val idToken: String) : LoginEvent
    data object NavegaParaRecuperarSenha                      : LoginEvent
}
