package com.app.gerenciadorcartoes.ui.feature.cadastrousuario

sealed interface CadastroUsuarioEvent {
    data class NomeAlterado(val valor: String)           : CadastroUsuarioEvent
    data class CpfAlterado(val valor: String)            : CadastroUsuarioEvent
    data class CepAlterado(val valor: String)            : CadastroUsuarioEvent
    data class EnderecoAlterado(val valor: String)       : CadastroUsuarioEvent
    data class NumberAlterado(val valor: String)         : CadastroUsuarioEvent
    data class BairroAlterado(val valor: String)         : CadastroUsuarioEvent
    data class EstadoAlterado(val valor: String)         : CadastroUsuarioEvent
    data class EmailAlterado(val valor: String)          : CadastroUsuarioEvent
    data class SenhaAlterada(val valor: String)          : CadastroUsuarioEvent
    data class ConfirmarSenhaAlterada(val valor: String) : CadastroUsuarioEvent

    data object Cadastrar                                : CadastroUsuarioEvent

    data object Voltar                                   : CadastroUsuarioEvent
}
