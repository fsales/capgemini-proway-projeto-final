package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.ChatMessage
import com.app.gerenciadorcartoes.model.ChatSender
import com.app.gerenciadorcartoes.network.model.MastraChatRequest
import com.app.gerenciadorcartoes.network.model.MastraGenerateResponse
import com.app.gerenciadorcartoes.network.service.MastraAgentService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class MastraChatRepositoryImplTest {

    @Test
    fun `enviarMensagem should send generate payload with invoices and memory`() = runTest {
        val service = FakeMastraAgentService(
            response = Response.success(MastraGenerateResponse(text = "Resposta do agente")),
        )
        val repository = MastraChatRepositoryImpl(service)

        val chunks = repository.enviarMensagem(
            historico = listOf(
                ChatMessage(id = 1L, sender = ChatSender.BOT, text = "Ola"),
                ChatMessage(id = 2L, sender = ChatSender.USER, text = "Quanto gastei?"),
            ),
            faturasJson = """[{"cartaoId":1,"total":100.0}]""",
            resourceId = "user-123",
        ).toList()

        val request = service.lastRequest

        assertEquals(listOf("Resposta do agente"), chunks)
        assertEquals("Quanto gastei?", request?.messages)
        assertTrue(request?.system.orEmpty().contains("Data atual: 2026-05-27"))
        assertTrue(request?.system.orEmpty().contains("""[{"cartaoId":1,"total":100.0}]"""))
        assertEquals("cartoes-user-123", request?.memory?.thread)
        assertEquals("user-123", request?.memory?.resource)
    }

    private class FakeMastraAgentService(
        private val response: Response<MastraGenerateResponse>,
    ) : MastraAgentService {

        var lastRequest: MastraChatRequest? = null
            private set

        override suspend fun generateCardSpendingAgent(
            request: MastraChatRequest,
        ): Response<MastraGenerateResponse> {
            lastRequest = request
            return response
        }
    }
}
