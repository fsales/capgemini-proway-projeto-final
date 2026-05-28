package com.app.gerenciadorcartoes.ui.feature.lista.chat

sealed interface ChatEvent {
    data object AbrirChat : ChatEvent
    data object FecharChat : ChatEvent
    data object EnviarMensagem : ChatEvent
    data class AlterarTexto(val texto: String) : ChatEvent
}
