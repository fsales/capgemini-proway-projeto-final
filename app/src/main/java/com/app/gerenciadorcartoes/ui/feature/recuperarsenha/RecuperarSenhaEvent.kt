package com.app.gerenciadorcartoes.ui.feature.recuperarsenha

sealed interface RecuperarSenhaEvent {
    data class  EmailAlterado(val valor: String) : RecuperarSenhaEvent
    data object Enviar                           : RecuperarSenhaEvent
    data object Voltar                           : RecuperarSenhaEvent
}
