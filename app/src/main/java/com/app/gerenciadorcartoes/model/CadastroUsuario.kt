package com.app.gerenciadorcartoes.model

data class CadastroUsuario(

    val nome: String = "",

    val cpf: String = "",

    val cep: Int = 0,

    val endereco: String = "",

    val number: String = "",

    val bairro: String = "",

    val estado: String = "",

    val email: String = "",

    val senha: String = "",
)
