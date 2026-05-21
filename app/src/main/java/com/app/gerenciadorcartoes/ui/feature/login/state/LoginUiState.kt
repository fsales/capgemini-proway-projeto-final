package com.app.gerenciadorcartoes.ui.feature.login.state

data class LoginUiState(
    val usuario     : String  = "",
    val senha       : String  = "",
    val carregando  : Boolean = false,
    val erroUsuario : String? = null,
    val erroSenha   : String? = null,
)
