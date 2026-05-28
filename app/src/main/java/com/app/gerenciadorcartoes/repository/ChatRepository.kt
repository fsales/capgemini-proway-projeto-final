package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun enviarMensagem(
        historico: List<ChatMessage>,
        faturasJson: String,
        resourceId: String,
    ): Flow<String>
}
