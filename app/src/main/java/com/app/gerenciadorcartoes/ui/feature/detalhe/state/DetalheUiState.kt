package com.app.gerenciadorcartoes.ui.feature.detalhe.state

import com.app.gerenciadorcartoes.model.Cartao

data class DetalheUiState(
    val cartao     : Cartao  = Cartao(),
    val carregando : Boolean = false,
    val erro       : String? = null,
)
