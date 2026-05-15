package com.app.gerenciadorcartoes.ui.feature.lista

// Eventos one-shot emitidos pelo ViewModel e consumidos uma única vez pela UI.
// Transportados via Channel para evitar reentrega após recomposição.
sealed interface ListaUiEvent {
    /** Navega para tela de detalhe/edição de item existente */
    data class  NavegaParaItem(val id: Long)          : ListaUiEvent
    /** Navega para tela de criação de novo item */
    data object NavegaParaNovo                         : ListaUiEvent
    /** Mostra um erro */
    data class  MostrarErro(val mensagem: String)     : ListaUiEvent
    /** Mostra uma mensagem */
    data class  MostrarMensagem(val mensagem: String) : ListaUiEvent
}

