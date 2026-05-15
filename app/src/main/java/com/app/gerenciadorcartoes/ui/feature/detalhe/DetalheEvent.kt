package com.app.gerenciadorcartoes.ui.feature.detalhe

sealed interface DetalheEvent {
    data object Voltar  : DetalheEvent
    data object Editar  : DetalheEvent
    data object Excluir : DetalheEvent
}
