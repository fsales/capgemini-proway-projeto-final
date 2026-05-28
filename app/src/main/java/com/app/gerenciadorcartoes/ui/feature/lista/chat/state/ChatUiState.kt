package com.app.gerenciadorcartoes.ui.feature.lista.chat.state

import com.app.gerenciadorcartoes.model.ChatMessage
import com.app.gerenciadorcartoes.model.ChatSender

data class ChatUiState(
    val aberto     : Boolean           = false,
    val mensagens  : List<ChatMessage> = conversaInicialChat(),
    val textoAtual : String            = "",
    val digitando  : Boolean           = false,
    val erro       : String?           = null,
) {
    val podeEnviar: Boolean get() = textoAtual.isNotBlank() && !digitando
}

fun conversaInicialChat(): List<ChatMessage> =
    listOf(
        ChatMessage(
            id     = 0L,
            sender = ChatSender.BOT,
            text   = "Ola! Sou o assistente de cartoes.",
        ),
    )
