package com.app.gerenciadorcartoes.ui.feature.lista.state

import com.app.gerenciadorcartoes.model.Cartao

data class ListaUiState(
    val cartoes      : List<Cartao> = emptyList(),
    val carregando   : Boolean      = false,
    val erro         : String?      = null,
    val nomeUsuario  : String?      = null,
    val userId       : String?      = null,
) {
    /** Primeiro nome extraído de [nomeUsuario] — usado na saudação da toolbar. */
    val primeiroNome: String? get() = nomeUsuario
        ?.split(" ")
        ?.firstOrNull()
        ?.takeIf { it.isNotBlank() }
}
