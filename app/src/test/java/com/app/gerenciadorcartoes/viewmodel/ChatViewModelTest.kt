package com.app.gerenciadorcartoes.viewmodel

import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.model.ChatMessage
import com.app.gerenciadorcartoes.model.FaturaMes
import com.app.gerenciadorcartoes.model.LancamentoFatura
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.repository.ChatRepository
import com.app.gerenciadorcartoes.repository.FaturaRepository
import com.app.gerenciadorcartoes.repository.SessaoRepository
import com.app.gerenciadorcartoes.ui.feature.lista.chat.ChatEvent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `enviarMensagem should send all card invoices to chat repository`() = runTest {
        val chatRepository = mockk<ChatRepository>()
        val cartaoRepository = mockk<CartaoRepository>()
        val faturaRepository = mockk<FaturaRepository>()
        val sessaoRepository = mockk<SessaoRepository>()
        val historicoSlot = mutableListOf<List<ChatMessage>>()
        val faturasSlot = mutableListOf<String>()
        val resourceSlot = mutableListOf<String>()

        coEvery { sessaoRepository.buscarUserId() } returns "user-123"
        coEvery { sessaoRepository.buscarIdUsuario() } returns 7L
        every { cartaoRepository.buscarCartaosPorUsuario(7L) } returns flowOf(
            listOf(
                Cartao(id = 1L, finalNumero = "1111", bandeira = "Visa", limite = 1000.0),
                Cartao(id = 2L, finalNumero = "2222", bandeira = "Elo", limite = 2000.0),
            ),
        )
        every { faturaRepository.listarFaturas(1L) } returns listOf(
            FaturaMes(
                competencia = YearMonth.of(2026, 5),
                lancamentos = listOf(
                    LancamentoFatura(
                        descricao = "Supermercado",
                        data = LocalDate.of(2026, 5, 10),
                        valor = 120.0,
                    ),
                ),
            ),
        )
        every { faturaRepository.listarFaturas(2L) } returns listOf(
            FaturaMes(
                competencia = YearMonth.of(2026, 5),
                lancamentos = listOf(
                    LancamentoFatura(
                        descricao = "Farmacia",
                        data = LocalDate.of(2026, 5, 11),
                        valor = 80.0,
                    ),
                ),
            ),
        )
        every {
            chatRepository.enviarMensagem(
                historico = capture(historicoSlot),
                faturasJson = capture(faturasSlot),
                resourceId = capture(resourceSlot),
            )
        } returns flowOf("Resposta do agente")

        val viewModel = ChatViewModel(
            chatRepository = chatRepository,
            cartaoRepository = cartaoRepository,
            faturaRepository = faturaRepository,
            sessaoRepository = sessaoRepository,
        )

        viewModel.onEvent(ChatEvent.AlterarTexto("Quanto gastei?"))
        viewModel.onEvent(ChatEvent.EnviarMensagem)
        advanceUntilIdle()

        assertEquals("Quanto gastei?", historicoSlot.single().last().text)
        assertEquals("user-123", resourceSlot.single())
        assertTrue(faturasSlot.single().contains("1111"))
        assertTrue(faturasSlot.single().contains("2222"))
        assertTrue(faturasSlot.single().contains("Supermercado"))
        assertTrue(faturasSlot.single().contains("Farmacia"))
        assertTrue(viewModel.uiState.value.mensagens.last().text.contains("Resposta do agente"))
    }

    class MainDispatcherRule(
        private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
