package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.model.ChatMessage
import com.app.gerenciadorcartoes.model.ChatSender
import com.app.gerenciadorcartoes.repository.CartaoRepository
import com.app.gerenciadorcartoes.repository.ChatRepository
import com.app.gerenciadorcartoes.repository.FaturaRepository
import com.app.gerenciadorcartoes.repository.SessaoRepository
import com.app.gerenciadorcartoes.ui.feature.lista.chat.ChatEvent
import com.app.gerenciadorcartoes.ui.feature.lista.chat.state.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository   : ChatRepository,
    private val cartaoRepository : CartaoRepository,
    private val faturaRepository : FaturaRepository,
    private val sessaoRepository : SessaoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var nextMessageId = 1L

    fun onEvent(event: ChatEvent) {
        when (event) {
            ChatEvent.AbrirChat       -> _uiState.update { it.copy(aberto = true) }
            ChatEvent.FecharChat      -> _uiState.update { it.copy(aberto = false) }
            ChatEvent.EnviarMensagem  -> enviarMensagem()
            is ChatEvent.AlterarTexto -> _uiState.update {
                it.copy(textoAtual = event.texto, erro = null)
            }
        }
    }

    private fun enviarMensagem() {
        val estadoAtual = _uiState.value
        if (estadoAtual.digitando) return

        val texto = estadoAtual.textoAtual.trim()
        if (texto.isBlank()) return

        val mensagemUsuario = ChatMessage(
            id     = proximoId(),
            sender = ChatSender.USER,
            text   = texto,
        )
        val mensagemBot = ChatMessage(
            id     = proximoId(),
            sender = ChatSender.BOT,
            text   = "",
        )
        val historico = estadoAtual.mensagens + mensagemUsuario

        _uiState.update {
            it.copy(
                mensagens  = historico + mensagemBot,
                textoAtual = "",
                digitando  = true,
                erro       = null,
            )
        }

        viewModelScope.launch {
            var recebeuChunk = false
            try {
                val contextoAgente = carregarContextoAgente()
                chatRepository.enviarMensagem(
                    historico   = historico,
                    faturasJson = contextoAgente.faturasJson,
                    resourceId  = contextoAgente.resourceId,
                ).collect { chunk ->
                    recebeuChunk = true
                    anexarTexto(mensagemBot.id, chunk)
                }
                if (!recebeuChunk) {
                    anexarTexto(mensagemBot.id, "Nao recebi uma resposta do assistente.")
                }
                _uiState.update { it.copy(digitando = false) }
            } catch (erro: Throwable) {
                if (erro is CancellationException) throw erro
                _uiState.update { estado ->
                    estado.copy(
                        digitando = false,
                        erro      = erro.message ?: "Erro ao conversar com o assistente.",
                        mensagens = estado.mensagens.map { mensagem ->
                            if (mensagem.id == mensagemBot.id && mensagem.text.isBlank()) {
                                mensagem.copy(text = "Nao consegui responder agora.")
                            } else {
                                mensagem
                            }
                        },
                    )
                }
            }
        }
    }

    private suspend fun carregarContextoAgente(): ChatAgentContext {
        val resourceId = sessaoRepository.buscarUserId()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_RESOURCE_ID
        val idUsuario = sessaoRepository.buscarIdUsuario()
        val cartoes = cartaoRepository.buscarCartaosPorUsuario(idUsuario).first()

        return ChatAgentContext(
            faturasJson = montarFaturasJson(cartoes),
            resourceId  = resourceId,
        )
    }

    private fun montarFaturasJson(cartoes: List<Cartao>): String =
        buildJsonArray {
            cartoes.forEach { cartao ->
                add(
                    buildJsonObject {
                        put("cartaoId", cartao.id)
                        put("finalNumero", cartao.finalNumero)
                        put("bandeira", cartao.bandeira)
                        put("limite", cartao.limite)
                        put("limiteMaximo", cartao.limiteMaximo)
                        put("bloqueado", cartao.bloqueado)
                        put(
                            "faturas",
                            buildJsonArray {
                                faturaRepository.listarFaturas(cartao.id).forEach { fatura ->
                                    add(
                                        buildJsonObject {
                                            put("competencia", fatura.competencia.toString())
                                            put("total", fatura.total)
                                            put(
                                                "lancamentos",
                                                buildJsonArray {
                                                    fatura.lancamentos.forEach { lancamento ->
                                                        add(
                                                            buildJsonObject {
                                                                put("descricao", lancamento.descricao)
                                                                put("data", lancamento.data.toString())
                                                                put("valor", lancamento.valor)
                                                            },
                                                        )
                                                    }
                                                },
                                            )
                                        },
                                    )
                                }
                            },
                        )
                    },
                )
            }
        }.toString()

    private fun anexarTexto(messageId: Long, chunk: String) {
        _uiState.update { estado ->
            estado.copy(
                mensagens = estado.mensagens.map { mensagem ->
                    if (mensagem.id == messageId) {
                        mensagem.copy(text = mensagem.text + chunk)
                    } else {
                        mensagem
                    }
                },
            )
        }
    }

    private fun proximoId(): Long = nextMessageId++

    private data class ChatAgentContext(
        val faturasJson : String,
        val resourceId  : String,
    )

    private companion object {
        const val DEFAULT_RESOURCE_ID = "user-local"
    }
}
