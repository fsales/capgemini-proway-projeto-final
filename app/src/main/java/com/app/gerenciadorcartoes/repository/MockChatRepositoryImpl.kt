package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.ChatMessage
import com.app.gerenciadorcartoes.model.ChatSender
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockChatRepositoryImpl @Inject constructor() : ChatRepository {

    override fun enviarMensagem(
        historico: List<ChatMessage>,
        faturasJson: String,
        resourceId: String,
    ): Flow<String> = flow {
        val pergunta = historico.lastOrNull { it.sender == ChatSender.USER }?.text.orEmpty()

        delay(450L)
        respostaMock(pergunta).chunked(CHUNK_SIZE).forEach { chunk ->
            emit(chunk)
            delay(70L)
        }
    }

    private fun respostaMock(pergunta: String): String {
        val texto = pergunta.lowercase()
        return when {
            "limite" in texto ->
                "Seu limite pode ser ajustado na tela de detalhes do cartão. Quando o agente Mastra estiver conectado, eu vou consultar o cartão selecionado e sugerir o melhor valor."
            "fatura" in texto || "faturas" in texto ->
                "Para faturas, abra um cartão e toque em ver faturas. Esta resposta esta vindo do mock com streaming local, pronta para trocar pelo endpoint do Mastra."
            "bloquear" in texto || "bloqueio" in texto ->
                "Voce pode bloquear ou desbloquear um cartão nos detalhes. Com o Mastra conectado, posso orientar a ação conforme o contexto do cartão."
            else ->
                "Sou o assistente de cartões em modo mock. Ja mantenho o historico desta conversa e respondo em streaming simulado enquanto o agente Mastra ainda nao foi definido."
        }
    }

    private companion object {
        const val CHUNK_SIZE = 18
    }
}
