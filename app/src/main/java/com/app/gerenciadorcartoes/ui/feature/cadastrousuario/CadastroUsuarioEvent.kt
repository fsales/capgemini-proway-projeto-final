package com.app.gerenciadorcartoes.ui.feature.cadastrousuario

sealed interface CadastroUsuarioEvent {
    data class NomeAlterado(val valor: String)           : CadastroUsuarioEvent
    data class CpfAlterado(val valor: String)            : CadastroUsuarioEvent
    data class CepAlterado(val valor: String)            : CadastroUsuarioEvent
    data class EnderecoAlterado(val valor: String)       : CadastroUsuarioEvent
    data class NumberAlterado(val valor: String)         : CadastroUsuarioEvent
    data class BairroAlterado(val valor: String)         : CadastroUsuarioEvent
    data class CidadeAlterada(val valor: String)         : CadastroUsuarioEvent
    data class EstadoAlterado(val valor: String)         : CadastroUsuarioEvent
    data class EmailAlterado(val valor: String)          : CadastroUsuarioEvent
    data class SenhaAlterada(val valor: String)          : CadastroUsuarioEvent
    data class ConfirmarSenhaAlterada(val valor: String) : CadastroUsuarioEvent

    /** Avança para a próxima etapa após validar a etapa atual. Na última etapa, submete o cadastro. */
    data object AvancarEtapa  : CadastroUsuarioEvent

    /** Retorna para a etapa anterior sem validação. */
    data object VoltarEtapa   : CadastroUsuarioEvent

    /** Informa que o foco no campo com erro foi concluído; limpa a flag [focarPrimeiroCampoComErro]. */
    data object FocoRealizado : CadastroUsuarioEvent

    data object Voltar        : CadastroUsuarioEvent
}
