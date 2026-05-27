package com.app.gerenciadorcartoes.network.model

import kotlinx.serialization.Serializable

/**
 * Corpo da requisição POST /cards.
 *
 * [idUsuario] identifica o usuário dono do cartão no servidor remoto.
 * Os demais campos espelham [com.app.gerenciadorcartoes.model.Cartao].
 */
@Serializable
data class AddCardRequest(
    val idUsuario   : String,
    val id    : String,
    val nomeTitular : String,
    val finalNumero : String,
    val bandeira    : String,
    val validade    : String,
    val limite      : Double,
    val template    : String,
    val bloqueado   : Boolean,
)
