package com.app.gerenciadorcartoes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gerenciadorcartoes.network.service.BuscaCep
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioEvent
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.CadastroUsuarioUiEvent
import com.app.gerenciadorcartoes.ui.feature.cadastrousuario.state.CadastroUsuarioUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CadastroUsuarioViewModel @Inject constructor(
    private val buscaCep: BuscaCep,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroUsuarioUiState())
   
    val uiState: StateFlow<CadastroUsuarioUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<CadastroUsuarioUiEvent>(Channel.BUFFERED)

    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: CadastroUsuarioEvent) {
        when (event) {
            is CadastroUsuarioEvent.NomeAlterado ->
                _uiState.update { it.copy(nome = event.valor) }

            is CadastroUsuarioEvent.CpfAlterado ->
                _uiState.update { it.copy(cpf = event.valor) }

            is CadastroUsuarioEvent.CepAlterado -> {
                val cepLimpo = event.valor.filter { it.isDigit() }
                _uiState.update { it.copy(cep = event.valor) }
                if (cepLimpo.length == 8) buscarCep(cepLimpo)
            }

            is CadastroUsuarioEvent.EnderecoAlterado ->
                _uiState.update { it.copy(endereco = event.valor) }

            is CadastroUsuarioEvent.NumberAlterado ->
                _uiState.update { it.copy(number = event.valor) }

            is CadastroUsuarioEvent.BairroAlterado ->
                _uiState.update { it.copy(bairro = event.valor) }

            is CadastroUsuarioEvent.EstadoAlterado ->
                _uiState.update { it.copy(estado = event.valor) }

            is CadastroUsuarioEvent.EmailAlterado ->
                _uiState.update { it.copy(email = event.valor) }

            is CadastroUsuarioEvent.SenhaAlterada ->
                _uiState.update { it.copy(senha = event.valor) }

            is CadastroUsuarioEvent.ConfirmarSenhaAlterada ->
                _uiState.update { it.copy(confirmarSenha = event.valor) }

            CadastroUsuarioEvent.Cadastrar -> cadastrar()

            CadastroUsuarioEvent.Voltar ->
                viewModelScope.launch { _uiEvent.send(CadastroUsuarioUiEvent.NavigateBack) }
        }
    }

    private fun buscarCep(cep: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(buscandoCep = true) }
            runCatching {
                val resposta = buscaCep.getCep(cep)
                if (resposta.isSuccessful) {
                    val body = resposta.body()
                    if (body != null ) {
                        _uiState.update {
                            it.copy(
                                endereco = body.logradouro,
                                bairro   = body.bairro,
                                estado   = body.uf,
                            )
                        }
                    } else {
                        _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro("CEP não encontrado"))
                    }
                } else {
                    _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro("CEP não encontrado"))
                }
            }.onFailure { erro ->
                _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro(erro.message ?: "Erro ao buscar CEP"))
            }
            _uiState.update { it.copy(buscandoCep = false) }
        }
    }

    private fun cadastrar() {
        viewModelScope.launch {
            runCatching {
                val s = _uiState.value
                if (s.senha != s.confirmarSenha) {
                    throw Exception("As senhas não coincidem")
                }
          
                _uiEvent.send(CadastroUsuarioUiEvent.MostrarMensagem("Cadastro realizado com sucesso!"))
                _uiEvent.send(CadastroUsuarioUiEvent.NavigateBack)
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(CadastroUsuarioUiEvent.MostrarErro(erro.message ?: "Erro ao cadastrar"))
            }
        }
    }
}

