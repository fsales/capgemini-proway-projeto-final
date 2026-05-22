package com.app.gerenciadorcartoes.model

data class CadastroUsuario(
    val id       : Long   = 0L,
    val nome     : String = "",
    val cpf      : String = "",
    val cep      : String = "",
    val endereco : String = "",
    val number   : String = "",
    val bairro   : String = "",
    val estado   : String = "",
    val email    : String = "",
    val senha    : String = "",
)
