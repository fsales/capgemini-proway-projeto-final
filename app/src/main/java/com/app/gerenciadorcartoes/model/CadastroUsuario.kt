package com.app.gerenciadorcartoes.model

data class CadastroUsuario(
    val id       : Long   = 0L,
    val userId   : String = "",
    val nome     : String = "",
    val cpf      : String = "",
    val cep      : String = "",
    val endereco : String = "",
    val number   : String = "",
    val bairro   : String = "",
    val cidade   : String = "",
    val estado   : String = "",
    val email    : String = "",
)
