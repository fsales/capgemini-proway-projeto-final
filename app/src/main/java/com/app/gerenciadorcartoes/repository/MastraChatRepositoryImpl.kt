package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.ChatMessage
import com.app.gerenciadorcartoes.model.ChatSender
import com.app.gerenciadorcartoes.network.model.MastraChatMemoryRequest
import com.app.gerenciadorcartoes.network.model.MastraChatRequest
import com.app.gerenciadorcartoes.network.service.MastraAgentService
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MastraChatRepositoryImpl @Inject constructor(
    private val mastraAgentService : MastraAgentService,
) : ChatRepository {

    override fun enviarMensagem(
        historico: List<ChatMessage>,
        faturasJson: String,
        resourceId: String,
    ): Flow<String> = flow {
        val pergunta = historico.lastOrNull { it.sender == ChatSender.USER }?.text.orEmpty()
        check(pergunta.isNotBlank()) { "Informe uma mensagem para o agente." }

        val response = mastraAgentService.generateCardSpendingAgent(
            request = MastraChatRequest(
                messages = pergunta,
                system   = montarSystemPrompt(faturasJson),
                memory   = MastraChatMemoryRequest(
                    thread   = "cartoes-$resourceId",
                    resource = resourceId,
                ),
            ),
        )

        if (!response.isSuccessful) {
            val raw = response.errorBody()?.string().orEmpty()
            error("Mastra erro ${response.code()}: $raw")
        }

        val text = response.body()?.text?.takeIf { it.isNotBlank() }
            ?: error("Mastra retornou resposta sem texto.")
        emit(text)
    }

    private fun montarSystemPrompt(faturasJson: String): String =
        """
        Data atual: 2026-05-27
        Faturas do usuario:
        $faturasJson
        """.trimIndent()
}
