package com.app.gerenciadorcartoes.ui.feature.cadastrousuario.state


data class CadastroUsuarioUiState(
    val nome           : String  = "",
    val cpf            : String  = "",
    val cep            : String  = "",
    val endereco       : String  = "",
    val number         : String  = "",
    val bairro         : String  = "",
    val estado         : String  = "",
    val email          : String  = "",
    val senha          : String  = "",
    val confirmarSenha : String  = "",
    val carregando     : Boolean = false,
    val buscandoCep     : Boolean = false
)
