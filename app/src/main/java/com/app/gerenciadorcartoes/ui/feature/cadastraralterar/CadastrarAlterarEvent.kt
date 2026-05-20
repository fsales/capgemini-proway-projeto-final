package com.app.gerenciadorcartoes.ui.feature.cadastraralterar

sealed interface CadastrarAlterarEvent {
    data object Voltar                                : CadastrarAlterarEvent
    data object Salvar                                : CadastrarAlterarEvent
    data class  NomeTitularAlterado(val valor: String): CadastrarAlterarEvent
    data class  FinalNumeroAlterado(val valor: String): CadastrarAlterarEvent
    data class  BandeiraAlterada(val valor: String)   : CadastrarAlterarEvent
    data class  ValidadeAlterada(val valor: String)   : CadastrarAlterarEvent
    data class  LimiteAlterado(val valor: String)     : CadastrarAlterarEvent
    data class  TemplateAlterado(val valor: String)   : CadastrarAlterarEvent
}

