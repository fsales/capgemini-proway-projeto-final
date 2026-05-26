package com.app.gerenciadorcartoes.ui.feature.recuperarsenha.state

data class RecuperarSenhaUiState(
    val email      : String  = "",
    val erroEmail  : String? = null,
    val enviando   : Boolean = false,
)

