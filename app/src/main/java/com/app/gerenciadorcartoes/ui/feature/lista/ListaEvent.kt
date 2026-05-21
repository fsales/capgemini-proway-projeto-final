package com.app.gerenciadorcartoes.ui.feature.lista

sealed interface ListaEvent {
    /** FAB acionado — usuário quer criar novo item */
    data object NavegaParaNovo : ListaEvent
    /** Clique em item existente — navega para detalhe */
    data class  NavegaParaItem(val id: Long) : ListaEvent
    /** Ação do menu — navega direto para edição */
    data class  NavegaParaEditar(val id: Long) : ListaEvent
    /** Exclui um item */
    data class  ExcluirCartao(val id: Long) : ListaEvent
    /** Usuário confirmou sair da conta */
    data object Deslogar : ListaEvent
}
