package com.app.gerenciadorcartoes.model

data class UsuarioAuth(
    val userId : String,   // id opaco — não "uid", não "firebaseUid"
    val email  : String?,  // null para provedores que não expõem email
    val nome   : String?,  // null para email/senha (Firebase não popula displayName nesses casos)
                           // o nome canônico do usuário vem sempre de CadastroUsuario.nome (Room)
)

