package com.app.gerenciadorcartoes.model

enum class ChatSender {
    USER,
    BOT,
}

data class ChatMessage(
    val id     : Long,
    val sender : ChatSender,
    val text   : String,
)
