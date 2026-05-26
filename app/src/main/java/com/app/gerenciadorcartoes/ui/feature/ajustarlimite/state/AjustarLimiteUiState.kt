package com.app.gerenciadorcartoes.ui.feature.ajustarlimite.state

data class AjustarLimiteUiState(
    val limiteAtual        : Double  = 0.0,
    val limiteMaximo       : Double  = 0.0,
    val novoLimite         : Double  = 0.0,
    val cartaoFinalNumero  : String  = "",
    val cartaoEncontrado   : Boolean = true,
    val carregando         : Boolean = false,
    val salvando           : Boolean = false,
    val mostrarConfirmacao : Boolean = false,
    val limiteConfirmacao  : Double  = 0.0,
    val erroLimite         : String? = null,
    val mensagem           : String? = null,
    val aviso              : String? = null,
)
