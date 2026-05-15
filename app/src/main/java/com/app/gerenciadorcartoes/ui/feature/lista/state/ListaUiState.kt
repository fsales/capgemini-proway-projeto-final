package com.app.gerenciadorcartoes.ui.feature.lista.state

import com.app.gerenciadorcartoes.model.Cartao

data class ListaUiState(
    val cartoes    : List<Cartao> = emptyList(),
    val carregando : Boolean      = false,
    val erro       : String?      = null,
)
